# Record

--------------------------------------------------------------------------------------------------------------------

Simple Recording class for Supercollider with silent cut mode. Record an audio bus to a file. 24bit wav.

--------------------------------------------------------------------------------------------------------------------


~audioBus1 = Bus.audio(s, 1);

~rec1 = Record("/Works_S/Sounds/test1.wav", ~audioBus1, true);
~rec1.monitorOnOff_(true);

~rec1.start;
~rec1.stop;

//you can just push cmd peroid instead of running .stop
