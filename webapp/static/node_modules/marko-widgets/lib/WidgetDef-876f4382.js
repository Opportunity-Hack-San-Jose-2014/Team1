$rmod.def("/marko-widgets@1.1.12/lib/WidgetDef", function(require, exports, module, __filename, __dirname) { function WidgetDef(config, endFunc) {
    this.module = config.module;
    this.id = config.id;
    this.assignedId = config.assignedId;
    this.config = config.config;
    this.scope = config.scope;
    this.events = config.events;
    this.parent = config.parent;
    this.children = [];
    this.end = endFunc;
}

WidgetDef.prototype = {
    addChild: function (widgetDef) {
        this.children.push(widgetDef);
    },
    elId: function (name) {
        if (arguments.length === 0) {
            return this.id;
        } else {
            return this.id + '-' + name;
        }
    }
};

module.exports = WidgetDef;
});