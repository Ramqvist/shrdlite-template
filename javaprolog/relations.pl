
%:-[world].
:-[entities].

%box(b).
floor(floor).



column_number(Index,Col) :- world(World),nth1(Index,World,Col).

entity_number(Index,Ent) :- column_number(_,C),nth1(Index,C,entity(Ent,_,_,_)).



above(E1,E2) :- same_column(E1,E2),entity_number(I1,E1),entity_number(I2,E2),I1>I2.
under(E1,E2) :- same_column(E1,E2),entity_number(I1,E1),entity_number(I2,E2),I1<I2.
right_of(E1,E2) :- column_number(I1,C1),column_number(I2,C2),I1>I2,member(E1,C1),member(E2,C2).
left_of(E1,E2) :- column_number(I1,C1),column_number(I2,C2),I1<I2,member(E1,C1),member(E2,C2).
holding(Entity).



same_column(E1,E2) :- column_number(_,Col),member(entity(E1,_,_,_),Col),member(entity(E2,_,_,_),Col).


%after_in_list(E1,E2,[E2|Tail]) :- member(E1,Tail).
%after_in_list(E1,E2,[_|Tail]) :- after_in_list(E1,E2,Tail).

%after_in_world(C1,C2,[C2|Tail]) :- member(C1,Tail).
%after_in_world(C1,C2,[_|Tail]) :- after_in_world(C1,C2,Tail).

head(X,[Head|Tail]) :- X=Head.


adjacent_entities(E1,E2,[entity(E1,_,_,_)|Tail]) :- head(entity(E2,_,_,_),Tail).
adjacent_entities(E1,E2,[entity(E2,_,_,_)|Tail]) :- head(entity(E1,_,_,_),Tail).
adjacent_entities(E1,E2,[_|Tail]) :- adjacent_entities(E1,E2,Tail).


adjacent_columns(I1,I2) :- X is abs(I1-I2), X=1.


on_top(E1,E2) :- above(E1,E2),column_number(_,C),adjacent_entities(E1,E2,C),not(box(E2)).
inside(E1,E2) :- above(E1,E2),column_number(_,C),adjacent_entities(E1,E2,C),box(E2).

right_or_left(E1,E2) :- right_of(E1,E2);left_of(E1,E2).

beside(E1,E2) :- right_or_left(E1,E2),column_number(I1,C1),column_number(I2,C2),member(E1,C1),member(E2,C2),
    adjacent_columns(I1,I2).
