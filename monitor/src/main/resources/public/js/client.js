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
      body: formData,
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

async function setValueUpdate(clientId, sensorId) {
  await getValueUpdate(clientId, sensorId);
  setInterval(function () {
    getValueUpdate(clientId, sensorId);
  }, 500);
}

async function getValueUpdate(clientId, sensorId) {
  const response = await fetch(getRootUrl() + "client/" + clientId + "/" + sensorId);

  if (response.status === 200) {
    const result = await response.json();
    document.getElementById(sensorId).value = result.VALUE;
  }
}

function includeFPGA() {
  if (document.getElementById("optionals").innerText.includes("FPGA")) {
    const xmlHttp = new XMLHttpRequest();
    xmlHttp.open("GET", "fpga/" + document.getElementById("CLIENT-ID").innerText, false);
    xmlHttp.send(null);
    const div = document.getElementById("PLACE");
    div.innerHTML = xmlHttp.responseText;
  }
}

function isIpAddress(text) {
  const ipPattern = /^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}(.*)$/;
  return ipPattern.test(text.split("/")[0]);
}

function checkIfIPAddress() {
  const childDivs = document.getElementById("optionals").getElementsByTagName("div");
  for (i = 0; i < childDivs.length; i++) {
    const childDiv = childDivs[i];
    const textContent = childDiv.textContent.trim();
    if (isIpAddress(textContent)) {
      childDiv.innerHTML = `<a href="http://${textContent}">${textContent}</a>`;
    }
  }
}
