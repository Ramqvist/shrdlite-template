
:-include(world).
:-include(entity).


%box(b).
floor(floor).



/*column_number(Index,Col) :- write('ENTER column_number with '),write(Index),write(', '),write(Col),nl,
    world(World),nth1(Index,World,Col),write('EXIT column_number with '),write(Index),write(', '),write(Col),nl.

entity_number(Index,Ent) :- write('ENTER entity_number with '),write(Index),write(', '),write(Ent),nl,
    column(C),nth1(Index,C,Ent),
    write('EXIT entity_number with '),write(Index),write(', '),write(Ent),nl.
*/

/*above(E1,E2) :- write('ENTER above with '),write(E1),write(', '),write(E2),nl,
    same_column(E1,E2),entity_number(I1,E1),entity_number(I2,E2),@>(I1,I2),
    write('EXIT above'),nl.*/
above(E1,E2) :- column(C),after_in_list(E1,E2,C).
under(E1,E2) :- column(C),before_in_list(E1,E2,C).
/*right_of(E1,E2) :- column_number(I1,C1),column_number(I2,C2),
    write(I1),write(' och '),write(I2),nl,
    @>(I1,I2),member(E1,C1),member(E2,C2).*/
right_of(E1,E2) :- world(W),after_in_world(E1,E2,W).
left_of(E1,E2) :- world(W),before_in_world(E1,E2,W).
holding(Entity).



%entity_index(Index,Entity) :- Index is .

above_test(E1,E2,Res) :- findall(E1, right_of(E1,E2), Res).


in_column(Index,Ent) :- column_number(Index,Col),member(Ent,Col).

%same_column(E1,E2) :- in_column(Index,E1),in_column(Index,E2).


same_column(E1,E2,Int) :- column_number(Int,C),member(E1,C),member(E2,C).

/*same_column(E1,E2) :- write('ENTER same_column with '),write(E1),write(', '),write(E2),nl,
    column_number(_,Col),write('bweh?'),nl,
    member(E1,Col),write(member(E1,Col)),nl,
    member(E2,Col),write('EXIT same_column'),nl.
*/



member_column(Elem,Col) :- write('ENTER member_column with '),write(Elem),write(', '),write(Col),nl,
    member(entity(Elem,_,_,_),Col),write('EXIT member_column'),nl.


bweh(E1,E2) :- world(C), apa(E1,E2,C).


after_in_list(E1,E2,[E2|Tail]) :- member(E1,Tail).
after_in_list(E1,E2,[_|Tail]) :- after_in_list(E1,E2,Tail).

before_in_list(E1,E2,[E1|Tail]) :- member(E2,Tail).
before_in_list(E1,E2,[_|Tail]) :- before_in_list(E1,E2,Tail).

after_in_world(E1,E2,[Head|Tail]) :- 
    member(E2,Head) -> memberr(E1,Tail)
    ; after_in_world(E1,E2,Tail).

before_in_world(E1,E2,[Head|Tail]) :-
    member(E1,Head) -> memberr(E2,Tail)
    ; before_in_world(E1,E2,Tail).


memberr(X, [L|_]) :- member(X, L). %X is member of first element
memberr(X, [_|T]) :- memberr(X, T). %X is member of tail


same_column(E1,E2) :- column(Col),before_in_list(E1,E2,Col).
same_column(E1,E2) :- column(Col),after_in_list(E1,E2,Col).


head(X,[Head|Tail]) :- X=Head.


adjacent_entities(E1,E2,[E1|Tail]) :- head(E2,Tail).
adjacent_entities(E1,E2,[E2|Tail]) :- head(E1,Tail).
adjacent_entities(E1,E2,[_|Tail]) :- adjacent_entities(E1,E2,Tail).



%apa(-1,_,[]).
apa(Index,Col,[Col|Tail]) :- Index is 1.
apa(Index,Col,[_|Tail]) :- apa(NewIndex,Col,Tail), Index is NewIndex+1.



/*
adjacent_columns(E1,E2,[Head|Tail]) :-
    member(E1,Head) -> head(C,Tail),member(E2,C) ;
    member(E2,Head) -> head(C,Tail),member(E1,C) 
    ; adjacent_columns(E1,E2,Tail).*/





on_top(E1,E2) :- above(E1,E2),column(C),adjacent_entities(E1,E2,C),\+(box(E2)).
inside(E1,E2) :- above(E1,E2),column(C),adjacent_entities(E1,E2,C),box(E2).

right_or_left(E1,E2) :- right_of(E1,E2);left_of(E1,E2).


beside(E1,E2) :- world(W),apa(I1,C1,W),apa(I2,C2,W),X is abs(I1-I2),X is 1,member(E1,C1),member(E2,C2).

