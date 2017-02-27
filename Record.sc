Record{
	var path, bus, buf, recordSyn, monitorSyn, monitorBuf, <>monitorOnOff = false, startedOrNot = false;

	*new {|path_, bus_, silentCutMode = false|
		^super.new.init(path_, bus_, silentCutMode);
	}

	init { |path_, bus_, silentCutMode|
		var s = Server.local;

		path = path_;
		bus = bus_;

		SynthDef("RecordMono", { arg buffer, buss;
			var busss = In.ar(buss, 1);
			DiskOut.ar(buffer, busss);
			if(silentCutMode==true, {DetectSilence.ar(busss, time:0.1, doneAction: 2)});
		}).send(s);

		SynthDef("MonitorIncomingBus", { var in;
			in = bus.ar(1);
			Out.ar(0, in);
			if(silentCutMode==true, {DetectSilence.ar(in, time:0.1, doneAction: 2)});
		}).send(s);


	}

	start{
		var s = Server.local;

		startedOrNot = true;
		buf = Buffer.alloc(s, 65536, 1);
		buf.write(path, "wav", "int24", 0, 0, true);

		recordSyn = Synth.new("RecordMono", [\buffer, buf, \buss, bus], s, 'addToTail');
		monitorBuf = Buffer.cueSoundFile(s, path, 0, 1);
		monitorSyn = Synth.new("MonitorIncomingBus", target: s, addAction:  'addToTail' );
		monitorOnOff = true;

			CmdPeriod.doOnce {
			    if(startedOrNot == true, {
				  monitorOnOff = false;
				  monitorSyn.free;
				  monitorBuf.freeMsg;
				  buf.close;
				  recordSyn.free;
				  buf.freeMsg;
			});
		};
	}


	stop{
		monitorOnOff = false;
		monitorSyn.free;
		monitorBuf.freeMsg;
		buf.close;
		recordSyn.free;
		buf.freeMsg;
	    startedOrNot = false;
	}

	monitor{|onOff|
		var s = Server.local;
			if(onOff == true,
				{
					if(monitorOnOff == true, { }, {
						monitorSyn = Synth.new("MonitorIncomingBus", target: s, addAction:  'addToTail' );
						monitorOnOff = true;
						});
				}, {
					if(monitorOnOff == false, { }, {
						monitorSyn.free; monitorOnOff = false;
					});
			});

	}

}

