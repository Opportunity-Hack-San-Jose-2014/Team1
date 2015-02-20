$rmod.def("/raptor-pubsub@1.0.5/lib/raptor-pubsub", function(require, exports, module, __filename, __dirname) { var EventEmitter = require('/$/marko-widgets/$/events'/*'events'*/).EventEmitter;

var channels = {};

var globalChannel = new EventEmitter();

globalChannel.channel = function(name) {
    var channel;
    if (name) {
        channel = channels[name] || (channels[name] = new EventEmitter());
    } else {
        channel = new EventEmitter();
    }
    return channel;
};

globalChannel.removeChannel = function(name) {
    delete channels[name];
};

module.exports = globalChannel;

});