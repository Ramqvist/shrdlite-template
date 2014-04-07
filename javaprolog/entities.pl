:-[world].

column(Col) :- world(W),member(Col,W).



box(X) :- entity_in_world(X,box).



entity_in_world(Id,Form) :- world(W),column(C),entity_in_column(Id,Form,C),column_in_world(C).


entity_in_column(Id,Form,Col) :- member(entity(Id,Form,_,_),Col).


column_in_world(Col) :- world(W), member(Col,W).




