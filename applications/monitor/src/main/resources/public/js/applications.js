async function uploadApplication(button) {
    let formData = new FormData();
    let clientID = button.id.replace("-app-button", "");

    let file = document.getElementById("hexFile")
    let sector = document.getElementById("sector").value

    formData.append("file", file.files[0]);
    formData.append("clientID", clientID);
    formData.append("sector", sector);

    let response = await fetch("/app/upload", {
        method: "POST",
        body: formData,
    });

    switch (response.status) {
        case 200:
            alert("Application received successfully.");
            break;
        case 400:
            alert("Application was not received by client.");
            break;
        default:
            alert("Unknown response status: " + response.status);
    }
}

async function rebootToPosition(button) {
    let formData = new FormData();
    let clientID = button.id.split("-app-button-")[0]

    let sector = button.id.split("-app-button-")[1]

    formData.append("clientID", clientID);
    formData.append("sector", sector);

    let response = await fetch("/app/reboot", {
        method: "POST",
        body: formData,
    });

    switch (response.status) {
        case 200:
            // alert("Command send successfully.");
            break;
        case 400:
            alert("Command not received by client.");
            break;
        default:
            alert("Unknown response status: " + response.status);
    }
}

