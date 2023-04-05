async function uploadFile(button) {
    let formData = new FormData();
    let twinID = button.id.replace("-flash-button", "");

    let file = document.getElementById("bitFile")

    formData.append("file", file.files[0]);
    formData.append("twinID", twinID);

    let response = await fetch("/upload", {
        method: "POST",
        body: formData,
    });

    console.log(response.status)

    switch (response.status) {
        case 200:
            let fileName = file.value.split("\\")
            alert("BitFile received successfully to.");
            break;
        case 400:
            alert("No file selected.");
            break;
        default:
            alert("Unknown response status: " + response.status);
    }
}

async function changeName(button) {
    let ID = button.id.replace("-name-button", "");
    let nameText = document.getElementById(ID + "-name");
    let newText = prompt("New Name:", nameText.innerText);
    if (newText == null || newText === "") {

    } else {
        let formData = new FormData();

        formData.append("ID", ID);
        formData.append("name", newText);
        let response = await fetch("/changeName", {
            method: "POST",
            body: formData
        });

        switch (response.status) {
            case 200:
                nameText.innerText = newText;
                break;
            default:
                alert("Unknown response status: " + response.status);
        }
    }
}

function getRootUrl() {
    return window.location.origin
        ? window.location.origin + "/"
        : window.location.protocol + "/" + window.location.host + "/";
}

async function setValueUpdate(deviceId, sensorId, fieldId) {
    await getValueUpdate(deviceId, sensorId, fieldId)
    const interval = setInterval(function() {
        getValueUpdate(deviceId, sensorId, fieldId)
    }, 5000);
}

async function getValueUpdate(deviceId, sensorId, fieldId) {
    console.log("value Update", deviceId);

    const response = await fetch(getRootUrl() + "sensor/" + deviceId + "/" + sensorId);

    if (response.status !== 200) {
        console.log(response.json());
        document.getElementById(fieldId).value = "HTTP Status: " + response.status;
        return;
    }

    const result = await response.json();
    document.getElementById(fieldId).value = result.VALUE;
}
