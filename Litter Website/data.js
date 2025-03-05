const mysql = require(`mysql-await`);
const bcrypt = require(`bcrypt`);

var connPool = mysql.createPool({
    connectionLimit: 5,
    host: "localhost",
    user: "C4131F23U85",
    database: "C4131F23U85",
    password: "7275"
});

// total variable has to be set by getTotalPosts, so total is never touched
// outside of this function 
let total = -1;
async function getTotalPosts(change) {
    if (total == -1) {
        let qstring = "SELECT COUNT(id) FROM posts";
        let retval = await connPool.awaitQuery(qstring);
        total = retval[0]['COUNT(id)'];
    }
    total += change;
    return total;
}

// posts table
async function getRecentPosts(page) {
    let size = await getTotalPosts(0);
    let offset = (page) * 10;
    if (offset > size) {
        return false;
    }
    let qstring = "SELECT * FROM posts ORDER BY posttime DESC LIMIT ?,10";
    let retval = await connPool.awaitQuery(qstring,[offset]);
    let morePages = false;
    if (size > offset + 10) {
        morePages = true;
    }
    return {posts: retval, morePages: morePages};
}

async function getLikedPosts(page) {
    let size = await getTotalPosts(0);
    let offset = (page) * 10;
    if (offset > size) {
        return false;
    }
    let qstring = "SELECT * FROM posts ORDER BY likes DESC LIMIT ?,10";
    let retval = await connPool.awaitQuery(qstring,[offset]);
    let morePages = false;
    if (size > offset + 10) {
        morePages = true;
    }
    return {posts: retval, morePages: morePages};
}

async function addPost(user,posttext) {
    let qstring = "INSERT INTO posts (user,posttext) VALUES (?,?)";
    let retval = await connPool.awaitQuery(qstring,[user,posttext]);
    if (retval.affectedRows == 1) {
        getTotalPosts(1);
        return true;
    } else {
        return false;
    }
}

async function editPost(id,posttext) {
    let qstring = "UPDATE posts SET posttext=?,edittime=CURRENT_TIMESTAMP WHERE id=?";
    let retval = await connPool.awaitQuery(qstring, [posttext,id]);
    return retval.affectedRows == 1; 
}

async function deletePost(id) {
    let qstring = "DELETE FROM posts WHERE id=?";
    let retval = await connPool.awaitQuery(qstring, [id]);
    if (retval.affectedRows == 1) {
        getTotalPosts(-1);
        return true;
    } else {
        return false; 
    }
}

async function likePost(id) {
    let qstring = "UPDATE posts SET likes = likes + 1 WHERE id=?";
    let retval = await connPool.awaitQuery(qstring, [id]);
    return retval.affectedRows == 1;
}

// logins table
async function addLogin(user, password) {
    let prestring = "SELECT COUNT(id) FROM logins WHERE user=?";
    let preval = await connPool.awaitQuery(prestring, [user]);
    if (preval[0]["COUNT(id)"] == 1) { return false; }
    let qstring = "INSERT INTO logins (user,password) VALUES (?,?)";
    const hpass = await bcrypt.hash(password, 10);
    let retval = await connPool.awaitQuery(qstring, [user,hpass]);
    return retval.affectedRows == 1;
}

async function checkLogin(user, password) {
    const qstring = "SELECT password FROM logins WHERE user=?";
    const retval = await connPool.awaitQuery(qstring, [user]);
    const valid = await bcrypt.compare(password, retval[0]["password"]);
    return valid;
}


module.exports = {getRecentPosts, getLikedPosts, addPost, editPost, deletePost, likePost, addLogin, checkLogin};