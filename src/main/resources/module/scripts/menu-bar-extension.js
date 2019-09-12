// Load the localization file
var dictionary = {};
$.ajax({
	url : "command/core/load-language?",
	type : "POST",
	async : false,
	data : {
	    module : "metadata"
	},
	success : function(data) {
		dictionary = data['dictionary'];
		lang = data['lang'];
	}
});
$.i18n().load(dictionary, lang);

// Extend the extensions menu bar
$(function(){
    ExtensionBar.MenuItems.push(
        {
            "id":"metadata",
                "label": $.i18n('menu-bar-extension/menu-label'),
                "submenu" : [
                    {
                        id: "metadata/post-fdp",
                        label: $.i18n("menu-bar-extension/post-fdp"),
                        click: function() { PostFDPInitialDialog.launch()  }
                    }
                ]
        }
    );
});
