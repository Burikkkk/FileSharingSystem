function copyToClipboard() {
    const copyText = document.getElementById("downloadLink");
    navigator.clipboard.writeText(copyText.textContent)
        .then(() => alert("Link copied!"))
        .catch(err => alert("Error copying"));
}

function downloadFromInput() {
    const linkInput = document.getElementById("manualLink").value;
    if (linkInput) {
        window.location.href = linkInput;
    } else {
        alert("Enter download link");
    }
}

const realFile = document.getElementById("realFile");
const customButton = document.getElementById("customButton");
const fileName = document.getElementById("fileName");

customButton.addEventListener("click", () => {
    realFile.click();
});

realFile.addEventListener("change", () => {
    if (realFile.files.length > 0) {
        fileName.textContent = realFile.files[0].name;
    } else {
        fileName.textContent = "No file selected";
    }
});
