
:-include(world).
:-include(entities).


%entity(X,Form,Size,Color) :- entity_desu(X,Form,Size,Color).%,form(Form),size(Size),color(Color).

%entity(Id,Form,Size,Color) :- valid_entity(Id,Form,Size,Color),column(C),


form(box).
form(plank).
form(ball).
form(table).
form(pyramid).



color(black).
color(white).
color(green).
color(blue).
color(red).
color(yellow).

size(small).
size(large).



column(Col) :- world(W),member(Col,W).
entityy(E) :- column(C), member(E,C).


box(X) :- entity_in_world(X,box).
ball(X) :- entity_in_world(X,ball).
plank(X) :- entity_in_world(X,plank).
brick(X) :- entity_in_world(X,brick).
table(X) :- entity_in_world(X,table).
pyramid(X) :- entity_in_world(X,pyramid).

small(X) :- entity_in_world(X,small).
large(X) :- entity_in_world(X,large).

same_size(E1,E2) :- (small(E1),small(E2);large(E1),large(E2)),\==(E1,E2).


exists(Form) :- once(form_in_world(Form)).


entity_in_world(Id) :- column(C),member(Id,C).

entity_in_world(Id,Form) :- entity_in_world(Id),entity_in_column(Id,Form,C).



form_in_world(Form) :- column(C),form_in_column(Form,C).




%entity_in_column(Id,Col) :- member(Id,Col).

entity_in_column(Id,Form,Col) :- form(Form), column(Col),member(Id,Col),once(entity(Id,Form,_,_)).
entity_in_column(Id,Size,Col) :- size(Size),column(Col),member(Id,Col),once(entity(Id,_,Size,_)).

form_in_column(Form,Col) :- entity(_,Form,_,_),member(Form,Col).






