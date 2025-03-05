(* An expression evaluator that allows for math expressions, 
    function definitions and recursion.
 *)


type value 
   = Int of int
  | Bool of bool
  | Closure of string * expr * environment

  | Ref of value ref
  | ListVal of value list

and environment = (string * value) list
                               
and expr 
  = Val of value

  | Add of expr * expr
  | Sub of expr * expr
  | Mul of expr * expr
  | Div of expr * expr
  | Neg of expr 

  | Lt  of expr * expr
  | Gt  of expr * expr
  | Eq  of expr * expr
  | And of expr * expr
  | Or  of expr * expr
  | Not of expr

  | Let of string * expr * expr
  | Id  of string

  | Lam of string * expr
  | App of expr * expr

  | LetRec of string * expr * expr
  | If of expr * expr * expr

  | Nil
  | Cons of expr * expr
  | Head of expr
  | Tail of expr
  | IsEmpty of expr

(* Part 1: free variables *)

let rec freevars (e: expr) : string list =
  match e with
  | Val _ | Nil -> []
  | Add (e1, e2) | Sub (e1, e2) | Mul (e1, e2) | Div (e1, e2) | Lt (e1, e2) 
  | Gt (e1, e2) | Eq (e1, e2) | And (e1, e2) | Or (e1, e2) | App (e1, e2) 
  | Cons (e1, e2) -> 
          (freevars e1) @ (freevars e2)
  | Neg (e1) | Not (e1) | Head (e1) | Tail (e1) | IsEmpty (e1) -> freevars e1
  | Let (x, e1, e2) -> (freevars e1) @ 
              (List.filter (fun x' -> x <> x') (freevars e2))
  | LetRec (x, e1, e2) ->
      (List.filter (fun x' -> x <> x') ((freevars e1) @ (freevars e2))) 
  | Id x -> [x]
  | Lam (x, e1) -> List.filter (fun x' -> x <> x') (freevars e1)
  | If (e1, e2, e3) -> (freevars e1) @ (freevars e2) @ (freevars e3)

(* Part 2: evaluation *)

exception DivisionByZero of value
exception UnboundVariable of string
exception IncorrectType of string

exception HeadOfEmptyList 
exception TailOfEmptyList 

let make_closure (var: string) (e: expr) (env: environment) : value =
  let get_val (name: string) : (string * value) =
    try ( List.find (fun (id, v) -> name = id) env ) with 
    | Not_found -> raise (UnboundVariable name)
  in
  let cl_env = List.map get_val (List.filter (fun x' -> var <> x') (freevars e))
  in 
  Closure (var, e, cl_env)

let rec eval (e:expr) (env: environment) : value =
  match e with
  | Val v -> v
  
  | Add (e1, e2) ->
     ( match eval e1 env, eval e2 env with
       | Int v1, Int v2 -> Int (v1 + v2)
       | _ -> raise (IncorrectType "Add")
     )
  | Sub (e1, e2) ->
    ( match eval e1 env, eval e2 env with
       | Int v1, Int v2 -> Int (v1 - v2)
       | _ -> raise (IncorrectType "Sub")
     )
  | Mul (e1, e2) ->
    ( match eval e1 env, eval e2 env with
       | Int v1, Int v2 -> Int (v1 * v2)
       | _ -> raise (IncorrectType "Mul")
     )
  | Div (e1, e2) ->
    ( match eval e1 env, eval e2 env with
       | Int v1, Int v2 when v2 = 0 -> raise (DivisionByZero (Int v1))
       | Int v1, Int v2 -> Int (v1 / v2)
       | _ -> raise (IncorrectType "Div")
     )
  | Neg e1 ->
    ( match eval e1 env with
       | Int v1 -> Int (0 - v1)
       | _ -> raise (IncorrectType "Neg")
     )

  | Lt (e1, e2) ->
    ( match eval e1 env, eval e2 env with
       | Int v1, Int v2 -> Bool (v1 < v2)
       | _ -> raise (IncorrectType "Lt")
     )
  | Gt (e1, e2) ->
    ( match eval e1 env, eval e2 env with
       | Int v1, Int v2 -> Bool (v1 > v2)
       | _ -> raise (IncorrectType "Gt")
     )
  | Eq (e1, e2) ->
    ( match eval e1 env, eval e2 env with
       | Int v1, Int v2 -> Bool (v1 = v2)
       | _ -> raise (IncorrectType "Eq")
     )
  | And (e1, e2) ->
    ( match eval e1 env, eval e2 env with
       | Bool v1, Bool v2 -> Bool (v1 && v2)
       | _ -> raise (IncorrectType "And")
     )
  | Or (e1, e2) ->
    ( match eval e1 env, eval e2 env with
       | Bool v1, Bool v2 -> Bool (v1 || v2)
       | _ -> raise (IncorrectType "or")
     )
  | Not (e1) ->
    ( match eval e1 env with
       | Bool v1 -> Bool (not v1)
       | _ -> raise (IncorrectType "Not")
     )

  | Let (name, v, in_e) -> eval in_e ((name, eval v env)::env)
  | Id (name) -> ( try 
      ( match List.find (fun (var, v) -> var = name) env with
        | (name, v) -> ( match v with
                        | Ref r -> !r
                        | _ -> v
                      )
      ) with
      | Not_found -> raise (UnboundVariable name)
    )

  | Lam (var, e1) -> make_closure var e1 env
  | App (e1, e2) ->
    ( match eval e1 env, eval e2 env with
    | Closure (var, e1, defs), x -> eval e1 ((var, x)::defs)
    | _ -> raise (IncorrectType "App")
  )

  | LetRec (name, fxn, e1) -> let recFxn = ref (Int 999) in
    let c = (eval fxn ((name, Ref (recFxn))::env)) in let () = recFxn := c in
    eval e1 ((name, Ref (recFxn))::env)
  | If (e1, e2, e3) ->
    ( match eval e1 env with
       | Bool v1 when v1 = true -> eval e2 env
       | Bool v1 -> eval e3 env
       | _ -> raise (IncorrectType "If")
    )

  | Nil -> ListVal []
  | Cons (e1, e2) ->
    ( match eval e1 env, eval e2 env with
    | x, ListVal l1 -> ListVal (List.cons x l1)
    | _ -> raise (IncorrectType "Cons")
    )
  | Head (e1) -> 
    ( match eval e1 env with
    | ListVal l1 when l1 = [] -> raise (HeadOfEmptyList)
    | ListVal l1 -> List.hd l1
    | _ -> raise (IncorrectType "Head")  
    )
  | Tail (e1) -> 
    ( match eval e1 env with
    | ListVal l1 when l1 = [] -> raise (TailOfEmptyList)
    | ListVal l1 -> ListVal (List.tl l1)
    | _ -> raise (IncorrectType "Tail")  
    )
  | IsEmpty (e1) -> 
    ( match eval e1 env with
    | ListVal l1 -> Bool (l1 = [])
    | _ -> raise (IncorrectType "IsEmpty")  
    )






(* Some sample expressions for testing *)

(* Arithmetic *)
let e01 = Add (Val (Int 3), Val (Int 5))
let e02 = Mul (Add (Val (Int 3), Val (Int 2)), 
               Sub (Val (Int 4), Val (Int 2)))
let e03 = Sub (Val (Int 8), Val (Int 2))
let e04 = Add (Val (Int 8), Neg (Val (Int 2)))


(* Relational and logical operations *)
let e05 = Lt (Add (Val (Int 3), Val (Int 5)), Val (Int 10))

let e06 = And (Lt (Val (Int 10), Val (Int 15)),
              Not (Eq (Val (Int 10), Val (Int 8))))

let e07 = Or (Gt (Val (Int 10), Val (Int 15)),
              Not (Eq (Val (Int 10), Val (Int 8))))



(* Let expressions *)

let e08 = Let ("x", Add (Val (Int 3), Val (Int 4)),
              Add (Id "x", Val (Int 5))
           )

   
let e09 = Let ("x", Add (Val (Int 3), Val (Int 4)),
              Lt (Id "x", Val (Int 5))
           )
       
(* ``let x = 3 < 5 in x && let x = 1 + 2 in x = 3 *)
let e10 = Let ("x",
              Lt (Val (Int 3), Val (Int 5)),
              And (Id "x",
                   Let ("x",
                        Add (Val (Int 1), Val (Int 2)),
                        Eq (Id "x", Val (Int 3))
                       )
                  )
             )


(* Functions *)
let e11 = Let ("inc", Lam ("n", Add (Id "n", Val (Int 1))),
               App (Id "inc", Val (Int 2)))

let e12 = Lam ("n", Add (Id "n", Val (Int 1)))

let e13 = Let ("add", 
               Lam ("x", Lam ("y", Add (Id "x", Id "y"))),
               App (App (Id "add", Val (Int 5)), Val (Int 3))
            )

let e14 = If (Eq (Val (Int 4), Val (Int 0)),
              Val (Int 0),
              Val (Int 1)
             )

let e15 = If (Eq (Val (Int 4), Val (Int 4)),
              Val (Bool true),
              Val (Bool false)
             )

let e16 = If (Eq (Val (Int 4), Val (Int 0)),
              Id ("x"),
              Val (Bool false)
             )

let sumToN : expr =
    LetRec ("sumToN",
            Lam ("n",
                 If (Eq (Id "n", Val (Int 0)),
                     Val (Int 0),
                     Add (Id "n",
                          App (Id "sumToN",
                               Sub (Id "n", Val (Int 1))
                              )
                         )
                    )
                ),
            Id "sumToN"
           )

let sumTo4 = App (sumToN, Val (Int 4))


let el01 = Cons (Add (Val (Int 1), Val (Int 2)),
                Cons (Add (Val (Int 3), Val (Int 4)),
                      Cons (Add (Val (Int 5), Val (Int 6)),
                            Nil)))
let el02 = Nil

let el03 = IsEmpty Nil

let el04 = IsEmpty (Cons (Add (Val (Int 1), Val (Int 2)),
                          Nil))

let el05 = Head (Cons (Add (Val (Int 1), Val (Int 2)),
                       Nil))

let el06 = Tail (Cons (Add (Val (Int 1), Val (Int 2)),
                       Cons (Add (Val (Int 3), Val (Int 4)),
                             Cons (Add (Val (Int 5), Val (Int 6)),
                                   Nil)))
             )

let sum : expr =
    LetRec ("sum",
            Lam ("lst",
                 If (IsEmpty (Id "lst"),
                     Val (Int 0),
                     Add (Head (Id "lst"),
                          App (Id "sum",
                               Tail (Id "lst")
                              )
                         )
                    )
                ),
            Id "sum"
           )

let sum_el01 = App (sum, el01)

(* Errors *)
(* Unbound Variables *)
let e20 = And (Id "y", Val (Bool true))


(* ``let x = 3 < 5 in y && let x = 1 + 2 in x = 3 *)
let e21 = Let ("x",
              Lt (Val (Int 3), Val (Int 5)),
              And (Id "y",
                   Let ("x",
                        Add (Val (Int 1), Val (Int 2)),
                        Eq (Id "x", Val (Int 3))
                       )
                  )
             )

(* let x = x + 4 in y && true *)
let e22 = Let ("x", Add (Id "x", Val (Int 4)),
                 And (Id "y", Val (Bool true))
              )



(* Division by Zero *)

let e23 = Let ("x", Val (Int 0), Div (Val (Int 4), Id "x"))


(* Type Errors *)
let e24 = Add (Val (Int 3), Val (Bool true))

let e25 = Let ("x", Add (Val (Int 3), Val (Int 4)),
                 And (Id "x", Val (Bool true))
              )


(* Unbound Variables *)
let e26 = Lam ("y", Add (Id "x", Id "y"))


(* Type Errors *)
let e27 = Let ("inc", Val (Int 1),
               App (Id "inc", Val (Int 2)))


let e28 = If (Add (Val (Int 3), Val (Int 5)),
              Val (Int 1),
              Val (Int 2) )



(* Type Errors *)
let el10 = IsEmpty (Add (Val (Int 1), Val (Int 2)))

let el11 = Head (Add (Val (Int 1), Val (Int 2)))

let el12 = Tail (Add (Val (Int 1), Val (Int 2)))

let el13 = Cons (Val (Int 1), Val (Int 2))




(* Functionality Testing *)
let _ =
  assert (List.sort compare (freevars e01) = []);
  assert (List.sort compare (freevars e08) = []);
  assert (List.sort compare (freevars e20) = ["y"]);
  assert (List.sort compare (freevars e21) = ["y"]);
  assert (List.sort compare (freevars e22) = ["x"; "y"]);


  assert (List.sort compare (freevars e11) = []);
  assert (List.sort compare (freevars e12) = []);
  assert (List.sort compare (freevars e13) = []);
  assert (List.sort compare (freevars e26) = ["x"]);

  assert (List.sort compare (freevars e14) = []);
  assert (List.sort compare (freevars e16) = ["x"]);

  assert (List.sort compare (freevars sumToN) = []);
  assert (List.sort compare (freevars sumTo4) = []);
  assert (List.sort compare (freevars sum) = []);
  assert (List.sort compare (freevars sum_el01) = [])

let _ =
  assert (eval e01 [] = Int 8);
  assert (eval e02 [] = Int 10);
  assert (eval e03 [] = Int 6);
  assert (eval e04 [] = Int 6);

  assert (eval e05 [] = Bool true);
  assert (eval e06 [] = Bool true);
  assert (eval e07 [] = Bool true);

  assert (eval e08 [] = Int 12);
  assert (eval e09 [] = Bool false);
  assert (eval e10 [] = Bool true);


  assert (eval e11 [] = Int 3);
  assert (eval e12 [] = Closure ("n", Add (Id "n", Val (Int 1)), []));
  assert (eval e13 [] = Int 8);

  assert (eval e26 [("x", Int 3)] =
            Closure ("y", Add (Id "x", Id "y"), [("x", Int 3)]));


  assert (eval e14 [] = Int 1);
  assert (eval e15 [] = Bool true);
  assert (eval e16 [] = Bool false);

  assert (match eval sumToN [] with
          | Closure ("n", _, [ ("sumToN", _) ]) -> true
          | _ -> false);

  assert (eval sumTo4 [] = Int 10);


  assert (eval el01 [] = ListVal [Int 3; Int 7; Int 11]);

  assert (eval el02 [] = ListVal []);

  assert (eval el03 [] = Bool true);

  assert (eval el04 [] = Bool false);

  assert (eval el05 [] = Int 3);

  assert (eval el06 [] = ListVal [Int 7; Int 11]);

  assert (match eval sum [] with
          | Closure ("lst", _, [ ("sum", _) ]) -> true
          | _ -> false);

  assert (eval sum_el01 [] = Int 21)

let _ = 

  assert (try (fun _ -> false) (eval e20 []) with
          | UnboundVariable "y" -> true
          | _ -> false);
  assert (try (fun _ -> false) (eval e21 []) with
          | UnboundVariable "y" -> true
          | _ -> false);
  assert (try (fun _ -> false) (eval e22 []) with
          | UnboundVariable v -> (v = "x") || (v = "y")
          | _ -> false);


  assert (try (fun _ -> false) (eval e26 []) with
          | UnboundVariable "x" -> true
          | _ -> false)
       (* This happens since we need to create a closure envirnoment
          that include "x" and its value - but "x" is not declared. *)



let _ =
  assert (try (fun _ -> false) (eval e23 []) with
          | DivisionByZero (Int 4) -> true
          | _ -> false)

let _ = 
  assert (try (fun _ -> false) (eval e24 []) with
          | IncorrectType "Add" -> true
          | _ -> false);

  assert (try (fun _ -> false) (eval e25 []) with
          | IncorrectType "And" -> true
          | _ -> false);


  assert (try (fun _ -> false) (eval e27 []) with
          | IncorrectType "App" -> true
          | _ -> false);

  assert (try (fun _ -> false) (eval e28 []) with
          | IncorrectType "If" -> true
          | _ -> false);

  assert (try (fun _ -> false) (eval el10[]) with
          | IncorrectType "IsEmpty" -> true
          | _ -> false);

  assert (try (fun _ -> false) (eval el11[]) with
          | IncorrectType "Head" -> true
          | _ -> false);

  assert (try (fun _ -> false) (eval el12[]) with
          | IncorrectType "Tail" -> true
          | _ -> false);

  assert (try (fun _ -> false) (eval el13[]) with
          | IncorrectType "Cons" -> true
          | _ -> false)


    