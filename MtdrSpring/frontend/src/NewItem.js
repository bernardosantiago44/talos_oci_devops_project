import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
import { useState } from "react";
function NewItem(props) {
    const [item, setItem] = useState('');
    function handleSubmit(e) {
        e.preventDefault();
        if (!item.trim())
            return;
        props.addItem(item);
        setItem("");
    }
    return (_jsxs("div", { className: "new-item-wrap", children: [_jsx("span", { className: "new-item-prefix", children: "+" }), _jsx("input", { className: "new-item-input", placeholder: "Add a new task\u2026", type: "text", autoComplete: "off", value: item, onChange: e => setItem(e.target.value), onKeyDown: e => { if (e.key === 'Enter')
                    handleSubmit(e); } }), _jsx("button", { className: "add-btn", disabled: props.isInserting || !item.trim(), onClick: handleSubmit, children: props.isInserting ? 'Adding…' : 'Add' })] }));
}
export default NewItem;
