async function createPost() {
    let post = document.getElementById("create").value;
    const result = await fetch("/api/post", {
        method:"POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({'posttext': post})
    });
    if (!result.ok) {
        let creationerror = document.getElementById("creationerror");
        const msg = result.json();
        creationerror.innerText = msg.res;
    } else {
        location.replace(location.href);
    }
}

async function deletePost(btn) {
    let post = btn.parentElement.parentElement;
    let postid = post.id;
    let id = parseInt(postid.substr(4));
    const result = await fetch("/api/post", {
        method: "DELETE",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({"id":id})
    });
    let errorspan = post.getElementsByClassName("errmsg")[0];
    if (!result.ok) {
        errorspan.innerText = "Error editing post";
        errorspan.classList.remove("hidden");
        setTimeout(() => {errorspan.classList.add("hidden");}, 5000);
    }
    location.replace(location.href);
}

async function editPost(btn) {
    btn.classList.add("hidden");
    let post = btn.parentElement.parentElement;
    let posttext = post.getElementsByClassName("post")[0];
    posttext.classList.add("hidden");
    let editarea = post.getElementsByClassName("editarea")[0];
    editarea.classList.remove("hidden");
    let submiteditbtn = post.getElementsByClassName("submiteditbtn")[0];
    submiteditbtn.classList.remove("hidden");
    let cancelbtn = post.getElementsByClassName("cancelbtn")[0];
    cancelbtn.classList.remove("hidden");
}

async function cancelEdit(btn) {
    btn.classList.add("hidden");
    let post = btn.parentElement.parentElement;
    let posttext = post.getElementsByClassName("post")[0];
    posttext.classList.remove("hidden");
    let editarea = post.getElementsByClassName("editarea")[0];
    editarea.value = posttext.innerText;
    editarea.classList.add("hidden");
    let editbtn = post.getElementsByClassName("editbtn")[0];
    editbtn.classList.remove("hidden");
    let submitbtn = post.getElementsByClassName("submiteditbtn")[0];
    submitbtn.classList.add("hidden");
}

async function submitEdit(btn) {
    let post = btn.parentElement.parentElement;
    let postid = post.id;
    let id = parseInt(postid.substr(4));
    let editarea = post.getElementsByClassName("editarea")[0];
    let posttext = editarea.value;
    const result = await fetch("/api/post", {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({"id":id, "posttext":posttext})
    });
    let errorspan = post.getElementsByClassName("errmsg")[0];
    if (!result.ok) {
        errorspan.innerText = "Error deleting post";
        errorspan.classList.remove("hidden");
        setTimeout(() => {errorspan.classList.add("hidden");}, 5000);
    }
    location.replace(location.href);
}

async function likePost(btn) {
    let post = btn.parentElement.parentElement;
    let postid = post.id;
    let id = parseInt(postid.substr(4));
    const result = await fetch("/api/liked", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({"id":id})
    });
    let errorspan = post.getElementsByClassName("errmsg")[0];
    if (!result.ok) {
        errorspan.innerText = "Error liking post";
        errorspan.classList.remove("hidden");
        setTimeout(() => {errorspan.classList.add("hidden");}, 5000);
    }
    errorspan.classList.add("hidden");
    let likespan = post.getElementsByClassName("likes")[0];
    let liketext = likespan.innerText;
    let likenum = parseInt(liketext.split(" ")[0]);
    likenum = likenum + 1;
    likespan.innerText = likenum + " Likes";
}