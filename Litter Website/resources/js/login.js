async function makeAccount() {
    let user = document.getElementById("user").value;
    let password = document.getElementById("password").value;
    const result = await fetch("/api/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({'user':user,'password':password})
    });
    let baduser = document.getElementById("baduser");
    let created = document.getElementById("accountcreated");
    if (!result.ok) {
        baduser.classList.remove("hidden");
        created.classList.add("hidden");
    } else {
        baduser.classList.add("hidden");
        created.classList.remove("hidden");
    }
}

async function tryLogin() {
    let user = document.getElementById("user").value;
    let password = document.getElementById("password").value;
    const result = await fetch("/api/login", {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({'user':user,'password':password})
    });
    let badlogin = document.getElementById("badlogin");
    if (!result.ok) {
        badlogin.classList.remove("hidden");
    } else {
        badlogin.classList.add("hidden");
        location.replace(location.href);
    }
}

async function logout() {
    const result = await fetch("/api/logout", {
        method: "GET"
    });
    if (!result.ok) {
        throw new Error("Error logging out");
    } else {
        location.replace(location.href);
    }
}