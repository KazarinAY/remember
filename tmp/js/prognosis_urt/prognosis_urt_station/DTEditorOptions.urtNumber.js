YUI.add('DTEditorOptions.urtNumber', function (Y) {

    Y.DataTable.EditorOptions.urtNumber = {
        BaseViewClass:  Y.DataTable.BaseCellPopupEditor,
        name:           'urtNumber',

        templateObject:{
            html: '<input type="text" title="inline cell editor" class="<%= this.classInput %>"  />'
        },
        inputWidth:'150px',
        overlayWidth:'170px',
        inputKeys:      true,

        /**
         * A validation regular expression object used to check validity of the input floating point number.
         * This can be defined by the user to accept other numeric input, or set to "null" to disable regex checks.
         *
         * @attribute validator
         * @type RegExp
         * @default /^\s*(\+|-)?((\d+(\.\d+)?)|(\.\d+))\s*$/
         */
        validator:  /^\s*(\+|-)?((\d+([\.,]\d+)?)|([\.,]\d+))\s*$/,
        //validator:  /^\s*(\+|-)?((\d+(\.\d+)?)|(\.\d+))\s*$/,

        keyFiltering:   /\.|,|[\x37]|[\b]|\d|\-|\+/,

        // Function to call after numeric editing is complete, prior to saving to DataTable ...
        //  i.e. checks validation against ad-hoc attribute "validationRegExp" (if it exists)
        //       and converts the value to numeric (or undefined if fails regexp);
       /* saveFn: function(v){
            var vre = this.get('validator'),
                value;
            v = v.replace(/,/,'.');
            if(vre instanceof RegExp) {
                value = (vre.test(v)) ? +v : undefined;
            } else {
                value = +v;
            }

            if(value === undefined) {
                value = "";
            }
            return value;
        },*/

        // Set an after listener to this View's instance
        after: {

            //---------
            // After this view is displayed,
            //   focus and "select" all content of the input (for quick typeover)
            //---------
            editorShow : function(o){
                var editor = this,
                //o.table.getRecord(0)
                    rowNum =  parseInt(o.table._openRecord._state.data.orderer.value),
                    table = this.get('table') || {},
                    masksConfig = this.get('editorMasks') || {},
                    stopEditableConfig = this.get('stopEditable') || [],
                    lists = this.get('lists') || [];
                o.inputNode.setAttribute('title','Введите значение');
                /**
                 * запрет на редактирование
                 */
                for(var i = 0; i < stopEditableConfig.length; i++) {
                    if(stopEditableConfig[i] == rowNum) {
                        this.hideEditor();
                        return;
                    }
                }

                o.inputNode.focus();
                o.inputNode.select();
            }
        }
    };

}, "0.0.1", { requires: ['widget', "base-build", "attribute"]})