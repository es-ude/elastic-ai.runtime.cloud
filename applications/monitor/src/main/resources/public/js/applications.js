async function uploadApplication(button) {
    let formData = new FormData();
    let clientID = button.id.replace("-app-button", "");

    let file = document.getElementById("hexFile")
    let appID = document.getElementById("appPos").value

    formData.append("file", file.files[0]);
    formData.append("clientID", clientID);
    formData.append("appID", appID);

    let response = await fetch("/app/upload", {
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

async function rebootToPosition(button) {
    let formData = new FormData();
    let clientID = button.id.replace("-app-button", "");

    let sector = document.getElementById("sector").value

    formData.append("clientID", clientID);
    formData.append("sector", sector);

    let response = await fetch("/app/reboot", {
        method: "POST",
        body: formData,
    });

    console.log(response.status)

    switch (response.status) {
        case 200:
            // alert("BitFile received successfully.");
            break;
        case 400:
            alert("not received by client.");
            break;
        default:
            alert("Unknown response status: " + response.status);
    }
}

