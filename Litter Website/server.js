const data = require("./data");

const session = require('express-session');
const express = require('express');
const app = express();
const port = 4131;

app.set("views", "templates");
app.set("view engine", "pug");

app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(session({
    secret: 'not twitter',
    saveUninitialized: true,
    resave: true,
    cookie: { 
        maxAge: 600000
    }
}))

app.use(express.static("resources"));

// ACTUAL PAGES

// main post page, default sorting by recent
app.get("/", async (req, res) => {
    let retval = await data.getRecentPosts(0);
    if (retval) {
        res.render("mainpage.pug", {posts: retval.posts,
            pagenum: 0,
            morepages: retval.morePages,
            liked: false,
            currentUser: req.session.loginuser});
    }
    else {
        res.status(500).render("500.pug", {location: "getting posts"});
    }
})

// get a certain page of posts, sorted by recent
app.get("/:pagenum([0-9]*)", async (req, res) => {
    let retval = await data.getRecentPosts(req.params.pagenum);
    if (retval) {
        res.render("mainpage.pug", {posts: retval.posts,
            pagenum: req.params.pagenum,
            morepages: retval.morePages,
            liked: false,
            currentUser: req.session.loginuser});
    }
    else {
        res.status(400).render("404.pug", {location: req.path});
    }
})

// get main post page, sorted by liked
app.get("/liked", async (req, res) => {
    let retval = await data.getLikedPosts(0);
    if (retval) {
        res.render("mainpage.pug", {posts: retval.posts,
            pagenum: 0,
            morepages: retval.morePages,
            liked: true,
            currentUser: req.session.loginuser});
    }
    else {
        res.status(500).render("500.pug", {location: "getting posts"});
    }
})

// get certain page of posts, sorted by liked
app.get("/liked/:pagenum([0-9]*)", async (req, res) => {
    let retval = await data.getLikedPosts(req.params.pagenum);
    if (retval) {
        res.render("mainpage.pug", {posts: retval.posts,
            pagenum: req.params.pagenum,
            morepages: retval.morePages,
            liked: true,
            currentUser: req.session.loginuser});
    }
    else {
        res.status(400).render("404.pug", {location: req.path});
    }
})


// API

// get posts by page sorted by recent
app.get("/api/post/:pagenum([0-9]*)", async (req, res) => {
    let retval = await data.getRecentPosts(req.params.pagenum);
    if (retval) {
        res.send({res: "success", data: retval});
    } else {
        res.status(500).send({res: "Something went wrong"});
    }
})

// get posts by page sorted by liked
app.get("/api/post/liked/:pagenum([0-9]*)", async (req, res) => {
    let retval = await data.getLikedPosts(req.params.pagenum);
    if (retval) {
        res.send({res: "success", data: retval});
    } else {
        res.status(500).send({res: "Something went wrong"});
    }
})

// post a new post
app.post("/api/post", async (req, res) => {
    if (req.session.loginuser == null) {
        res.status(400).send({res: "User is not logged in"});
    } else if (req.body.posttext == null) {
        res.status(400).send({res: "Missing post text in request body"});
    } else {
        let retval = await data.addPost(req.session.loginuser, req.body.posttext);
        if (retval) {
            res.status(201).send({res: "success"});
        } else {
            res.status(500).send({res: "Couldn't make post"});
        }
    }
})

// delete a post
app.delete("/api/post", async (req, res) => {
    if (req.body.id == null) {
        res.status(400).send({res: "Missing id in request body"});
    } else {
        let retval = await data.deletePost(req.body.id);
        if (retval) {
            res.send({res: "success"});
        } else {
            res.status(400).send({res: "Couldn't find post"});
        }
    }
})

// edit a post
app.put("/api/post", async (req, res) => {
    if (req.body.id == null) {
        res.status(400).send({res: "Missing id in request body"});
    } else if (req.body.posttext == null) {
        res.status(400).send({res: "Missing post text in request body"});
    } else {
        let retval = await data.editPost(req.body.id, req.body.posttext);
        if (retval) {
            res.send({res: "success"});
        } else {
            res.status(400).send({res: "Couldn't find post"});
        }
    }
})

app.post("/api/liked", async (req, res) => {
    if (req.body.id == null) {
        res.status(400).send({res: "Missing id in request body"});
    } else {
        let retval = await data.likePost(req.body.id);
        if (retval) {
            res.send({res: "success"});
        } else {
            res.status(400).send({res: "Couldn't find post"})
        }
    }
})

// create a login
app.post("/api/login", async (req, res) => {
    if (req.body.user == null) {
        res.status(400).send({res: "Missing username in request body"});
    } else if (req.body.password == null) {
        res.status(400).send({res: "Missing password in request body"});
    } else {
        let retval = await data.addLogin(req.body.user, req.body.password);
        if (retval) {
            res.send({res: "success"});
        } else {
            res.status(400).send({res: "Invalid username"});
        }
    }
})

// attempt a login
app.put("/api/login", async (req, res) => {
    if (req.body.user == null) {
        res.status(400).send({res: "Missing username in request body"});
    } else if (req.body.password == null) {
        res.status(400).send({res: "Missing password in request body"});
    } else {
        let retval = await data.checkLogin(req.body.user, req.body.password);
        if (retval) {
            req.session.loginuser = req.body.user;
            res.send({res: "success"});
        } else {
            res.status(400).send({res: "Incorrect login"});
        }
    }
})

// attempt a logout
app.get("/api/logout", async (req, res) => {
    req.session.destroy();
    res.send({res: "success"});
})


// catch 404s
app.use((req, res, next) => {
    res.status(404).render("404.pug", req.path);
})

// start server
app.listen(port, () => {
    console.log(`Server listening on port ${port}`);
})