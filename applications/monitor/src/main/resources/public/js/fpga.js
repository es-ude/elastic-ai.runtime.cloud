async function uploadFile(button) {
  let formData = new FormData();
  let clientID = button.id.replace("-flash-button", "");

  let file = document.getElementById("bitFile");
  let startSectorID = document.getElementById("startSectorID").value;

  formData.append("file", file.files[0]);
  formData.append("clientID", clientID);
  formData.append("startSectorID", startSectorID);

  let response = await fetch("/bitfile/upload", {
    method: "POST",
    body: formData,
  });

  console.log(response.status);

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
