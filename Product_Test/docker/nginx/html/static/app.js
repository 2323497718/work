async function requestJson(url) {
    const resp = await fetch(url);
    return await resp.json();
}

document.getElementById("queryBtn").addEventListener("click", async () => {
    const id = document.getElementById("productId").value.trim() || "1";
    const result = document.getElementById("result");
    try {
        const data = await requestJson(`/api/products/${id}`);
        result.textContent = JSON.stringify(data, null, 2);
    } catch (e) {
        result.textContent = "请求失败: " + e.message;
    }
});

document.getElementById("statsBtn").addEventListener("click", async () => {
    const stats = document.getElementById("stats");
    try {
        const data = await requestJson("/api/system/stats");
        stats.textContent = JSON.stringify(data, null, 2);
    } catch (e) {
        stats.textContent = "请求失败: " + e.message;
    }
});
