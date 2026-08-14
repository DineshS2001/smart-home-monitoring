import {
    initializeApp
} from "https://www.gstatic.com/firebasejs/11.10.0/firebase-app.js";

import {
    getDatabase,
    onValue,
    ref,
    serverTimestamp,
    update
} from "https://www.gstatic.com/firebasejs/11.10.0/firebase-database.js";

const firebaseConfig = {
    apiKey: "AIzaSyD-yXlvR3txE2fZZajlavQsQDDWO37oEFg",
    authDomain: "smart-home-monitoring-36dc7.firebaseapp.com",
    databaseURL:
        "https://smart-home-monitoring-36dc7-default-rtdb." +
        "asia-southeast1.firebasedatabase.app",
    projectId: "smart-home-monitoring-36dc7",
    storageBucket: "smart-home-monitoring-36dc7.firebasestorage.app",
    messagingSenderId: "831423655678",
    appId: "1:831423655678:web:7509d76ca24748aa49ffaf"
};

const firebaseApp = initializeApp(firebaseConfig);
const database = getDatabase(firebaseApp);

const devicesReference = ref(database, "smartHome/devices");
const connectionReference = ref(database, ".info/connected");

const connectionStatus = document.querySelector("#connection-status");
const deviceGrid = document.querySelector("#device-grid");
const deviceTemplate = document.querySelector("#device-card-template");
const loadingMessage = document.querySelector("#loading-message");
const errorMessage = document.querySelector("#error-message");
const floorFilter = document.querySelector("#floor-filter");

const totalDevicesElement = document.querySelector("#total-devices");
const activeDevicesElement = document.querySelector("#active-devices");
const warningDevicesElement = document.querySelector("#warning-devices");

let allDevices = [];
let selectedFloor = "all";

onValue(connectionReference, (snapshot) => {
    const isConnected = snapshot.val() === true;

    connectionStatus.textContent = isConnected
        ? "Firebase connected"
        : "Disconnected";

    connectionStatus.className = isConnected
        ? "connection-status connected"
        : "connection-status disconnected";
});

onValue(
    devicesReference,
    (snapshot) => {
        const deviceObject = snapshot.val() || {};

        allDevices = Object.entries(deviceObject).map(
            ([deviceId, device]) => ({
                ...device,
                id: device.id || deviceId
            })
        );

        loadingMessage.classList.add("hidden");
        errorMessage.classList.add("hidden");

        renderSimulator();
    },
    (error) => {
        loadingMessage.classList.add("hidden");

        errorMessage.textContent =
            `Unable to load Firebase devices: ${error.message}`;

        errorMessage.classList.remove("hidden");
    }
);

floorFilter.addEventListener("change", (event) => {
    selectedFloor = event.target.value;
    renderSimulator();
});

function renderSimulator() {
    updateSummary();

    const visibleDevices = selectedFloor === "all"
        ? allDevices
        : allDevices.filter(
            (device) => device.floorId === selectedFloor
        );

    deviceGrid.replaceChildren();

    if (visibleDevices.length === 0) {
        const emptyState = document.createElement("div");

        emptyState.className = "empty-state";
        emptyState.textContent =
            "No simulated devices were found for this floor.";

        deviceGrid.appendChild(emptyState);
        return;
    }

    visibleDevices
        .sort((first, second) =>
            first.name.localeCompare(second.name)
        )
        .forEach((device) => {
            deviceGrid.appendChild(createDeviceCard(device));
        });
}

function updateSummary() {
    totalDevicesElement.textContent = String(allDevices.length);

    const activeCount = allDevices.filter(
        (device) => device.status === "ON"
    ).length;

    const warningCount = allDevices.filter(
        (device) =>
            device.status === "ERROR" ||
            device.status === "DISCONNECTED"
    ).length;

    activeDevicesElement.textContent = String(activeCount);
    warningDevicesElement.textContent = String(warningCount);
}

function createDeviceCard(device) {
    const fragment = deviceTemplate.content.cloneNode(true);
    const card = fragment.querySelector(".device-card");
    const roomElement = fragment.querySelector(".device-room");
    const nameElement = fragment.querySelector(".device-name");
    const typeElement = fragment.querySelector(".device-type");
    const statusElement = fragment.querySelector(".device-status");
    const controlsElement = fragment.querySelector(".device-controls");

    roomElement.textContent = device.roomName || "Unknown room";
    nameElement.textContent = device.name || "Unnamed device";
    typeElement.textContent = formatDeviceType(device.type);

    statusElement.textContent = device.status || "OFF";
    statusElement.classList.add(
        String(device.status || "OFF").toLowerCase()
    );

    if (device.type === "MULTI_SWITCH") {
        renderMultiSwitchControls(device, controlsElement);
    } else {
        renderStandardControl(device, controlsElement);
    }

    return card;
}

function renderStandardControl(device, container) {
    const controlRow = document.createElement("div");
    const label = document.createElement("span");
    const button = document.createElement("button");

    controlRow.className = "control-row";
    label.className = "control-label";
    button.className = "toggle-button";

    label.textContent = getControlLabel(device.type);

    const isOn = device.status === "ON";
    const canControl =
        device.status !== "ERROR" &&
        device.status !== "DISCONNECTED";

    button.textContent = isOn ? "ON" : "OFF";
    button.classList.toggle("on", isOn);
    button.disabled = !canControl;

    button.addEventListener("click", async () => {
        button.disabled = true;

        const newStatus = isOn ? "OFF" : "ON";

        try {
            await updateDeviceStatus(device, newStatus);
        } catch (error) {
            showError(`Device update failed: ${error.message}`);
            button.disabled = false;
        }
    });

    controlRow.append(label, button);
    container.appendChild(controlRow);
}

function renderMultiSwitchControls(device, container) {
    const switchCount = Number(device.numberOfSwitches || 1);
    const switchStates = device.switches || {};

    const masterRow = createSwitchRow(
        "Master control",
        Object.values(switchStates).some(Boolean),
        async (isOn) => {
            const changes = {
                status: isOn ? "ON" : "OFF"
            };

            for (let number = 1; number <= switchCount; number += 1) {
                changes[`switches/switch_${number}`] = isOn;
            }

            await update(
                ref(database, `smartHome/devices/${device.id}`),
                changes
            );
        }
    );

    container.appendChild(masterRow);

    for (let number = 1; number <= switchCount; number += 1) {
        const switchKey = `switch_${number}`;
        const currentState = switchStates[switchKey] === true;

        const switchRow = createSwitchRow(
            `Switch ${number}`,
            currentState,
            async (isOn) => {
                const updatedStates = {
                    ...switchStates,
                    [switchKey]: isOn
                };

                await update(
                    ref(database, `smartHome/devices/${device.id}`),
                    {
                        [`switches/${switchKey}`]: isOn,
                        status: Object.values(updatedStates).some(Boolean)
                            ? "ON"
                            : "OFF"
                    }
                );
            }
        );

        container.appendChild(switchRow);
    }
}

function createSwitchRow(labelText, isOn, onChange) {
    const row = document.createElement("div");
    const label = document.createElement("span");
    const button = document.createElement("button");

    row.className = "control-row";
    label.className = "control-label";
    button.className = "toggle-button";

    label.textContent = labelText;
    button.textContent = isOn ? "ON" : "OFF";
    button.classList.toggle("on", isOn);

    button.addEventListener("click", async () => {
        button.disabled = true;

        try {
            await onChange(!isOn);
        } catch (error) {
            showError(`Switch update failed: ${error.message}`);
            button.disabled = false;
        }
    });

    row.append(label, button);
    return row;
}

async function updateDeviceStatus(device, newStatus) {
    const changes = {
        status: newStatus
    };

    if (device.type === "IRON") {
        changes.turnedOnAt =
            newStatus === "ON" ? serverTimestamp() : null;
    }

    await update(
        ref(database, `smartHome/devices/${device.id}`),
        changes
    );
}

function getControlLabel(type) {
    switch (type) {
        case "CAMERA":
            return "Camera power";

        case "IRON":
            return "Safety outlet";

        case "LIGHT":
            return "Light power";

        case "OUTLET":
            return "Power supply";

        default:
            return "Device power";
    }
}

function formatDeviceType(type) {
    switch (type) {
        case "MULTI_SWITCH":
            return "Multi-switch unit";

        case "CAMERA":
            return "Security camera";

        case "IRON":
            return "Safety-controlled appliance";

        case "LIGHT":
            return "Smart light";

        case "OUTLET":
            return "Electrical outlet";

        default:
            return "Smart device";
    }
}

function showError(message) {
    errorMessage.textContent = message;
    errorMessage.classList.remove("hidden");

    window.setTimeout(() => {
        errorMessage.classList.add("hidden");
    }, 5000);
}