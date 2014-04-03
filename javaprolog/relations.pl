
:-[world].

box(b).
floor(floor).



column(Index,Col) :- world(World),nth1(Index,World,Col).



above(E1,E2) :- column(_,C),after_in_list(E1,E2,C).
under(Entity1,Entity2) :- not(floor(Entity1)).
right_of(E1,E2) :- column(I1,C1),column(I2,C2),I1>I2,member(E1,C1),member(E2,C2).
left_of(E1,E2) :- column(I1,C1),column(I2,C2),I1<I2,member(E1,C1),member(E2,C2).
holding(Entity).



% same_column(E1,E2) :- column(Col),member(E1,Col),member(E2,Col).


after_in_list(E1,E2,[E2|Tail]) :- member(E1,Tail).
after_in_list(E1,E2,[_|Tail]) :- after_in_list(E1,E2,Tail).

after_in_world(C1,C2,[C2|Tail]) :- member(C1,Tail).
after_in_world(C1,C2,[_|Tail]) :- after_in_world(C1,C2,Tail).

head(X,[Head|Tail]) :- X=Head.


adjacent_entities(E1,E2,[E1|Tail]) :- head(E2,Tail).
adjacent_entities(E1,E2,[E2|Tail]) :- head(E1,Tail).
adjacent_entities(E1,E2,[_|Tail]) :- adjacent_entities(E1,E2,Tail).


adjacent_columns(I1,I2) :- X is abs(I1-I2), X=1.


on_top(E1,E2) :- above(E1,E2),column(_,C),adjacent_entities(E1,E2,C).
inside(E1,E2) :- above(E1,E2),column(_,C),adjacent_entities(E1,E2,C),box(E2).

right_or_left(E1,E2) :- right_of(E1,E2);left_of(E1,E2).

beside(E1,E2) :- right_or_left(E1,E2),column(I1,C1),column(I2,C2),member(E1,C1),member(E2,C2),adjacent_columns(I1,I2).
