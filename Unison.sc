Unison {
    var <voices, <detune, <blend, <phaseRand, <mode;
    var ratios, amp;

    *new {|voices=7, detune=0.01, blend=0.75, phaseRand=1.0, mode=\linear|
        ^super.newCopyArgs(voices, detune, blend, phaseRand, mode)
    }

    voices_ { |newValue|
        # voices, ratios, amp = [newValue];
    }

    detune_ { |newValue|
        # detune, ratios, amp = [newValue];
    }

    blend_ { |newValue|
        # blend, amp = [newValue];
    }

    phaseRand_ { |newValue|
        # phaseRand = [newValue];
    }

    mode_ { |newValue|
        var modes = Set[\linear, \exponential];
        if(modes.includes(newValue).not, {Error("mode must be in " + modes).throw;});
        # mode, ratios, amp = [newValue]
    }

    ratios {
        if(ratios == nil, {
            switch(mode)
            {\exponential} {
                var detuneMul = 1.0 + detune;
                ratios = voices.collect{|i| i.linexp(0, voices-1, detuneMul.reciprocal, detuneMul, nil)};}
            {\linear} {
                ratios = voices.collect{|i| i.linlin(0, voices-1, 1-detune, 1+detune, nil);}}
            {Error("unknown Unison mode").throw;};
            ratios = ratios.perfectShuffle;
            voices.odd.if({ ratios.insert(voices.half.asInteger, 1.0) });
        });
        ^ratios
    }

    amp {
        if (amp == nil, {
            if(detune==0, {
                amp = Array.fill(voices, 1);
            }, {
                amp = this.ratios.collect{|ratio|
                    var ampRatio = ratio;
                    if(ampRatio > 1, {ampRatio = ampRatio.reciprocal;});
                    ampRatio
                }.linexp((1+detune).reciprocal, 1, blend, 1, nil);
            });
            amp = amp.normalizeSum(1);
        });
        ^amp;
    }

    iphase { |osc|
        var mul = switch(osc)
        {SinOsc} {2pi}
        {LFCub} {2}
        {LFPar} {4}
        {LFSaw} {2}
        {LFTri} {4}
        // The following are 0..1, so they don't need to be specified
        // LFPulse, VarSaw
        {1};
        ^{Rand(hi: mul * phaseRand)}!voices
    }

    printOn { |stream|
        stream << "Unison("
        << "voices: " << voices
        << ", detune: " << detune
        << ", blend: " << blend
        << ", mode: " << mode
        <<  ")";
    }
}
