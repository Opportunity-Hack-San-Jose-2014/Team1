$rmod.def("/marko-widgets@1.1.12/lib/uniqueId-browser", function(require, exports, module, __filename, __dirname) { var nextUniqueId = 0;

module.exports = function() {
    return 'c' + nextUniqueId++;
};
});