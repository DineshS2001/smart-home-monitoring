const { initializeApp, cert } = require("firebase-admin/app");
const {
  getDatabase,
  ServerValue
} = require("firebase-admin/database");

const serviceAccount = require("./serviceAccountKey.json");

const DATABASE_URL =
  "https://smart-home-monitoring-36dc7-default-rtdb.asia-southeast1.firebasedatabase.app";

initializeApp({
  credential: cert(serviceAccount),
  databaseURL: DATABASE_URL
});

const database = getDatabase();
const devicesReference = database.ref("smartHome/devices");
const alertsReference = database.ref("smartHome/alerts");

const activeTimers = new Map();

console.log("Smart Home safety worker is starting...");

devicesReference.on(
  "value",
  (snapshot) => {
    const devices = snapshot.val() || {};

    Object.entries(devices).forEach(([deviceId, device]) => {
      processSafetyDevice(deviceId, device);
    });
  },
  (error) => {
    console.error("Firebase listener failed:", error.message);
  }
);

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
      deviceId: deviceId,
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

process.on("SIGINT", () => {
  console.log("\nStopping safety worker...");

  activeTimers.forEach((timer) => {
    clearTimeout(timer);
  });

  process.exit(0);
});