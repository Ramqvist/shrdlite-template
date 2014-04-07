:-include(relations).

%ball_pos(Ball,E2,Valid) :- ball(Ball),\+(inside(Ball,E2)),Valid is -1.
%ball_pos(Ball,E2,Valid) :- column(C),ball(Ball),\+(apa(1,Ball,C)),Valid is -1.
%ball_pos(Ball,E2,Valid) :- Valid is 1.

ball_pos(Ball,E2,Valid) :- ball(Ball),\+(ball_pos_permitted(Ball,E2)),Valid is -1.
ball_pos(_,_,1).

ball_pos_permitted(Ball,E2) :- inside(Ball,E2);column(C),apa(1,Ball,C).

ball_supporting(E1,Ball,Valid) :- on_top(E1,Ball),ball(Ball),Valid is -1.
ball_supporting(_,_,1).

small_supporting(E1,Small,Valid) :- small(Small),large(E1),on_top(E1,Small),Valid is -1.
small_supporting(E1,Small,Valid) :- small(Small),large(E1),inside(E1,Small),Valid is -1.
small_supporting(_,_,1).


boxes_contain(Object,Box,Valid) :- box(Box),(pyramid(Object);plank(Object)),inside(Object,Box),
    same_size(Object,Box),Valid is -1.
boxes_contain(_,_,1).
