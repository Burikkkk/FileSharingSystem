function copyToClipboard() {
    const copyText = document.getElementById("downloadLink");
    navigator.clipboard.writeText(copyText.textContent)
        .then(() => alert("Ссылка скопирована!"))
        .catch(err => alert("Ошибка при копировании"));
}

function downloadFromInput() {
    const linkInput = document.getElementById("manualLink").value;
    if (linkInput) {
        window.location.href = linkInput;
    } else {
        alert("Введите ссылку для скачивания");
    }
}