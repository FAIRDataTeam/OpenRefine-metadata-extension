/* global $, lang, ExtensionBar, PostFdpDialog */

// Load the localization file
var dictionary = {};
var language = lang;
$.ajax({
    url : "command/core/load-language?",
    type : "POST",
    async : false,
    data : {
        module : "metadata"
    },
    success(data) {
        dictionary = data["dictionary"];
        language = data["lang"];
    }
});
$.i18n().load(dictionary, language);

// Extend the extensions menu bar
$(function(){
    ExtensionBar.MenuItems.push(
        {
            "id":"metadata",
                "label": $.i18n("menu-bar-extension/menu-label"),
                "submenu" : [
                    {
                        id: "metadata/post-fdp",
                        label: $.i18n("menu-bar-extension/post-fdp"),
                        click: PostFdpDialog.launch
                    }
                ]
        }
    );
});
