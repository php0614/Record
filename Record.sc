Record{
	var instNum;
	var path, bus, buf, recordSyn, monitorSyn, monitorBuf, <>monitorOnOff = true, startedOrNot = false, channelNum_;

	*new {|path_, bus_, channelNum = 1, silentCutMode = false|
		^super.new.init(path_, bus_, channelNum, silentCutMode);
	}

	init { |path_, bus_, channelNum, silentCutMode|
		var s = Server.local;

		instNum = 99999.rand;
		path = path_;
		bus = bus_;
		channelNum_ = channelNum;

		SynthDef("RecordMono_"++instNum.asString, { arg buffer, buss;
			var busss = In.ar(buss, channelNum);
			DiskOut.ar(buffer, busss);
			if(silentCutMode==true, {DetectSilence.ar(busss, time:0.1, doneAction: 2)});
		}).send(s);

		SynthDef("MonitorIncomingBus_"++instNum.asString, { var in;
			in = bus.ar(channelNum);
			Out.ar(0, in);
			if(silentCutMode==true, {DetectSilence.ar(in, time:0.1, doneAction: 2)});
		}).send(s);


	}

	start{
		var s = Server.local;

		startedOrNot = true;
		buf = Buffer.alloc(s, 65536, channelNum_);
		buf.write(path, "wav", "int24", 0, 0, true);

		recordSyn = Synth.new("RecordMono_"++instNum.asString, [\buffer, buf, \buss, bus], s, 'addToTail');
		monitorBuf = Buffer.cueSoundFile(s, path, 0, channelNum_);
		monitorSyn = Synth.new("MonitorIncomingBus_"++instNum.asString, target: s, addAction:  'addToTail' );
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
						monitorSyn = Synth.new("MonitorIncomingBus_"++instNum.asString, target: s, addAction:  'addToTail' );
						monitorOnOff = true;
						});
				}, {
					if(monitorOnOff == false, { }, {
						monitorSyn.free; monitorOnOff = false;
					});
			});

	}

}

