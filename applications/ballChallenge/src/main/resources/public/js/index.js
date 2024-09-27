async function measurementStart() {
  await fetch("/start", {
    method: "POST",
  });
}

async function setID(id) {
  let formData = new FormData();
  formData.append("id", id);
  await fetch("/setID", {
    method: "POST",
    body: formData,
  });
}

async function setCountDownUpdate() {
  setInterval(function () {
    getValueUpdate();
  }, 500);
}

async function getValueUpdate() {
  const responseTime = await fetch("/requestCountDownUpdate");
  if (responseTime.status === 200) {
    const result = await responseTime.json();
    document.getElementById("countdown").value = result.VALUE;
  }
  const responseGValue = await fetch("/requestGValueUpdate");
  if (responseGValue.status === 200) {
    const result = await responseGValue.json();
    document.getElementById("gvalue").value = result.VALUE;
  }
}
