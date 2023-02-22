// async function uploadFile(button) {
//     let formData = new FormData();
//     let twinURI = button.id.replace("-upload-button", "")
//
//     let file = document.getElementById(twinURI + "-select-file")
//     console.log(file)
//     formData.append("file", file.files[0]);
//     formData.append("twinURI", twinURI);
//
//     let response = await fetch("/upload", {
//         method: "POST",
//         body: formData,
//     });
//
//     let actionText = document.getElementById(twinURI + "-action-text");
//     switch (response.status) {
//         case 200:
//             let fileName = file.value.split("\\")
//             actionText.innerText = "Send file \"" + fileName[fileName.length - 1] + "\"";
//             updateLastAction(twinURI);
//             // alert("File send successfully to deviceTwin: " + deviceURI + ".");
//             break;
//         case 400:
//             alert("No file selected.");
//             break;
//         default:
//             alert("Unknown response status: " + response.status);
//     }
// }

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

// function sleep(ms) {
//     return new Promise(resolve => setTimeout(resolve, ms));
// }

// async function updateLastAction(URI) {
//     let statusText = document.getElementById(URI + "-action-text");
//     let flashing = true
//     while (flashing) {
//         let formData = new FormData();
//         formData.append("URI", URI);
//         let response = await fetch("/getStatus", {
//             method: "POST",
//             body: formData,
//         });
//         response.text().then(function (text) {
//             console.log(text)
//             statusText.innerText = text;
//             if (text.indexOf("Successfully flashed") === 0) {
//                 flashing = false;
//             }
//         });
//
//         await sleep(1000);
//     }
// }

function getRootUrl() {
    return window.location.origin
        ? window.location.origin + "/"
        : window.location.protocol + "/" + window.location.host + "/";
}

async function getValueUpdate(deviceId, sensorId, fieldId) {
    const response = await fetch(getRootUrl() + "sensor/" + deviceId + "/" + sensorId);

    if (response.status !== 200) {
        console.log(response.json());
        document.getElementById(fieldId).value = "HTTP Status: " + response.status;
        return;
    }

    const result = await response.json();
    document.getElementById(fieldId).value = result.VALUE;
}