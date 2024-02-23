async function measurementStart(button) {
    let formData = new FormData();
    let clientID = button.id.replace("-measurement-start-button", "");

    formData.append("clientID", clientID);

    let response = await fetch("/sensor/measurement/start/" + clientID, {
        method: "POST",
    });

    console.log(response.status)
}

async function uploadFile(button) {
    let formData = new FormData();
    let clientID = button.id.replace("-flash-button", "");

    let file = document.getElementById("bitFile")
    let startSectorID = document.getElementById("startSectorID").value

    formData.append("file", file.files[0]);
    formData.append("clientID", clientID);
    formData.append("startSectorID", startSectorID);

    let response = await fetch("/bitfile/upload", {
        method: "POST",
        body: formData,
    });

    console.log(response.status)

    switch (response.status) {
        case 200:
            alert("BitFile received successfully.");
            break;
        case 400:
            alert("Bitfile was not received by client.");
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

async function setValueUpdate(clientId, sensorId, fieldId) {
    await getValueUpdate(clientId, sensorId, fieldId)
    setInterval(function() {
        getValueUpdate(clientId, sensorId, fieldId)
    }, 1000);
}

async function getValueUpdate(clientId, sensorId, fieldId) {
    console.log("value Update", clientId);

    const response = await fetch(getRootUrl() + "sensor/" + clientId + "/" + sensorId);

    if (response.status === 200) {
        const result = await response.json();
        document.getElementById(fieldId).value = result.VALUE;
    }
}
