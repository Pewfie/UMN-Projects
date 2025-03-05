# 4131 Final Project
### Carson Hensold - henso055

## How to install dependencies
#### Run `npm install` in the command line inside the project directory.

## How to run the server
#### First start an ssh tunnel into a umn cselabs machine. Then connect to the mysql database with the command `mysql -u C4131F23U1 C4131F23U1 -p -h cse-mysql-classes-01.cse.umn.edu` (adjust for your username and database). After the ssh tunnel is set up, run `node server.js` in the command line.

## How to use the website
#### Direct the browser to `localhost:4131` and use the buttons within the site to access other pages.

### Website Behavior
#### Sorting:
- The buttons labelled "Recent Posts" and "Top Posts" sort by newest and by most liked, respectively.

#### Liking:
- The buttons in the bottom-right section of the posts labelled "Like" allow the user to like a post. There is no limit set on how many times a user can like a post currently, however a user cannot like their own post unless they log out.

#### Account Creation:
- The area in the top right of the site is dedicated to user accounts. To create an account, simply type a username into the input labelled "Username" and a password into the input labelled "Password" then click "Create Account".
- An account must have a unique username, so if the inputted username is invalid, the site will show "Invalid Username" in response.
- If the account is created, the site will show that through text.

#### Logging In:
- In the same area in the top right of the site, you can log in. To log in, type a username into the input labelled "Username" and a password into the input labelled "Password" then click "Login".
- If they are both correct, the server will identify the session with the supplied username, and the page will reload.
- If they are not correct, the page will show a message indicating so.

#### Post Creation:
- Once a user is logged in, they can make a post. A text area will appear above the sort buttons, in which up to 250 characters can be typed. 
- Once some text is typed and the submit button is pressed, a post will be created and added to the database. The page is then reloaded.

#### Deleting:
- If a user is logged in, they can delete any past posts they made. A delete button will appear underneath the timestamp of the post. 
- Pressing this delete button will delete the post from the database and reload the page.

#### Editing:
- If a user is logged in, they can edit any past posts they made. An edit button will appear underneath the timestamp of the post. 
- Pressing this edit button will replace the post with a text area that hold the current text of the post.
- The user can change that text to anything (up to 250 characters) and then hit submit to change the text of the post, and reload the page. The post will also be noted as "Edited at " the time it was edited at, rather than "Posted at" the original post time. However the sorting is still done by original post time.
- The edit button will change to a cancel button after edit is pressed, which can be pressed to get rid of the text box and submit button and return the page to normal. If there are any in-progress edits when cancel is pressed, they are reverted.

#### Logging out:
- If a user is logged in, the top right of the site will show their username and a log out button, instead of the username and password inputs and login/account creation buttons. 
- If a user wants to log out, they press the button labelled "Logout"

### Password hashing
#### Within data.js, the bcrypt library is used for password hashing. The database only stores the hash, and the check for logging in uses the compare function in bcrypt. Outside of creating an account and logging in, the user never sends a password.