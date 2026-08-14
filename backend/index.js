const { initializeApp, cert } = require("firebase-admin/app");

const {
  getDatabase,
  ServerValue
} = require("firebase-admin/database");

const serviceAccount = require("./serviceAccountKey.json");

const DATABASE_URL =
  "https://smart-home-monitoring-36dc7-default-rtdb." +
  "asia-southeast1.firebasedatabase.app";

const SCHEDULE_TIME_ZONE = "Asia/Colombo";
const SCHEDULE_CHECK_INTERVAL = 30 * 1000;

initializeApp({
  credential: cert(serviceAccount),
  databaseURL: DATABASE_URL
});

const database = getDatabase();
const devicesReference = database.ref("smartHome/devices");
const alertsReference = database.ref("smartHome/alerts");

const usageEventsReference =
  database.ref("smartHome/usageEvents");

const activeTimers = new Map();

const previousStatuses = new Map();
let statusTrackingInitialized = false;

console.log("Smart Home backend worker is starting...");
console.log(`Schedule time zone: ${SCHEDULE_TIME_ZONE}`);

devicesReference.on(
  "value",
  (snapshot) => {
    const devices = snapshot.val() || {};

    trackDeviceStatusChanges(devices);

    Object.entries(devices).forEach(([deviceId, device]) => {
      processSafetyDevice(deviceId, device);
    });
  },
  (error) => {
    console.error("Firebase listener failed:", error.message);
  }
);

function trackDeviceStatusChanges(devices) {
  if (!statusTrackingInitialized) {
    Object.entries(devices).forEach(([deviceId, device]) => {
      previousStatuses.set(
        deviceId,
        device.status || "UNKNOWN"
      );
    });

    statusTrackingInitialized = true;
    console.log("Device usage tracking initialized");
    return;
  }

  Object.entries(devices).forEach(([deviceId, device]) => {
    const newStatus = device.status || "UNKNOWN";
    const previousStatus = previousStatuses.get(deviceId);

    previousStatuses.set(deviceId, newStatus);

    if (
      previousStatus !== undefined &&
      previousStatus !== newStatus
    ) {
      recordUsageEvent(
        deviceId,
        device,
        previousStatus,
        newStatus
      );
    }
  });

  for (const deviceId of previousStatuses.keys()) {
    if (!devices[deviceId]) {
      previousStatuses.delete(deviceId);
    }
  }
}

async function recordUsageEvent(
  deviceId,
  device,
  previousStatus,
  newStatus
) {
  try {
    await usageEventsReference.push({
      deviceId,
      deviceName: device.name || "Unknown Device",
      deviceType: device.type || "UNKNOWN",
      floorId: device.floorId || "",
      previousStatus,
      newStatus,
      changedAt: ServerValue.TIMESTAMP
    });

    console.log(
      `${device.name || deviceId}: usage event recorded ` +
        `${previousStatus} → ${newStatus}`
    );
  } catch (error) {
    console.error(
      `${device.name || deviceId}: usage event failed:`,
      error.message
    );
  }
}

function processSafetyDevice(deviceId, device) {
  if (device.type !== "IRON") {
    return;
  }

  clearExistingTimer(deviceId);

  if (
    device.status !== "ON" ||
    !device.turnedOnAt ||
    !device.maxOnDurationMinutes
  ) {
    console.log(`${device.name || deviceId}: safety timer inactive`);
    return;
  }

  const maximumDurationMilliseconds =
    device.maxOnDurationMinutes * 60 * 1000;

  const elapsedMilliseconds =
    Date.now() - Number(device.turnedOnAt);

  const remainingMilliseconds =
    maximumDurationMilliseconds - elapsedMilliseconds;

  if (remainingMilliseconds <= 0) {
    performSafetyCutoff(deviceId, device);
    return;
  }

  console.log(
    `${device.name || deviceId}: cutoff scheduled in ` +
      `${Math.ceil(remainingMilliseconds / 1000)} seconds`
  );

  const timer = setTimeout(() => {
    performSafetyCutoff(deviceId, device);
  }, remainingMilliseconds);

  activeTimers.set(deviceId, timer);
}

function clearExistingTimer(deviceId) {
  const existingTimer = activeTimers.get(deviceId);

  if (existingTimer) {
    clearTimeout(existingTimer);
    activeTimers.delete(deviceId);
  }
}

async function performSafetyCutoff(deviceId, scheduledDevice) {
  try {
    activeTimers.delete(deviceId);

    const deviceReference = devicesReference.child(deviceId);
    const latestSnapshot = await deviceReference.get();
    const latestDevice = latestSnapshot.val();

    if (
      !latestDevice ||
      latestDevice.status !== "ON" ||
      latestDevice.turnedOnAt !== scheduledDevice.turnedOnAt
    ) {
      console.log(
        `${scheduledDevice.name || deviceId}: ` +
          "cutoff cancelled because state changed"
      );
      return;
    }

    await deviceReference.update({
      status: "OFF",
      turnedOnAt: null,
      lastSafetyCutoffAt: ServerValue.TIMESTAMP
    });

    await alertsReference.push({
      deviceId,
      deviceName: latestDevice.name || "Safety Device",
      type: "SAFETY_CUTOFF",
      title: "Device automatically turned off",
      message:
        `${latestDevice.name || "Device"} exceeded its ` +
        `${latestDevice.maxOnDurationMinutes}-minute safety limit.`,
      createdAt: ServerValue.TIMESTAMP,
      read: false
    });

    console.log(
      `${latestDevice.name || deviceId}: automatically turned OFF`
    );
  } catch (error) {
    console.error(
      `${scheduledDevice.name || deviceId}: cutoff failed:`,
      error.message
    );
  }
}

function getCurrentScheduleHour() {
  const hourText = new Intl.DateTimeFormat(
    "en-US",
    {
      timeZone: SCHEDULE_TIME_ZONE,
      hour: "2-digit",
      hourCycle: "h23"
    }
  ).format(new Date());

  return Number.parseInt(hourText, 10);
}

function isHourInsideSchedule(
  currentHour,
  startHour,
  endHour
) {
  if (startHour === endHour) {
    return false;
  }

  if (startHour < endHour) {
    return (
      currentHour >= startHour &&
      currentHour < endHour
    );
  }

  return (
    currentHour >= startHour ||
    currentHour < endHour
  );
}

async function checkLightSchedules() {
  try {
    const snapshot = await devicesReference.get();
    const devices = snapshot.val() || {};
    const currentHour = getCurrentScheduleHour();

    for (const [deviceId, device] of Object.entries(devices)) {
      if (
        device.type !== "LIGHT" ||
        device.scheduleEnabled !== true
      ) {
        continue;
      }
      if (
        device.status === "ERROR" ||
        device.status === "DISCONNECTED"
      ) {
        continue;
      }

      const startHour = Number(device.scheduleStartHour);
      const endHour = Number(device.scheduleEndHour);

      if (
        !Number.isInteger(startHour) ||
        !Number.isInteger(endHour)
      ) {
        console.error(
          `${device.name || deviceId}: invalid schedule hours`
        );

        continue;
      }

      const shouldBeOn = isHourInsideSchedule(
        currentHour,
        startHour,
        endHour
      );

      const requiredStatus = shouldBeOn ? "ON" : "OFF";

      if (device.status !== requiredStatus) {
        await devicesReference.child(deviceId).update({
          status: requiredStatus,
          lastScheduleActionAt: ServerValue.TIMESTAMP
        });

        console.log(
          `${device.name || deviceId}: schedule changed status to ` +
            `${requiredStatus} at ${currentHour}:00`
        );
      }
    }
  } catch (error) {
    console.error(
      "Light schedule check failed:",
      error.message
    );
  }
}

const scheduleInterval = setInterval(
  checkLightSchedules,
  SCHEDULE_CHECK_INTERVAL
);

setTimeout(checkLightSchedules, 2000);

process.on("SIGINT", () => {
  console.log("\nStopping backend worker...");

  activeTimers.forEach((timer) => {
    clearTimeout(timer);
  });

  clearInterval(scheduleInterval);
  process.exit(0);
});