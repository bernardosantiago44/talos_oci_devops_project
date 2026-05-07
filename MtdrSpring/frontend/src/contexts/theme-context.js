import { jsx as _jsx } from "react/jsx-runtime";
import { createContext, useContext, useEffect, useState } from 'react';
const ThemeContext = createContext({ theme: 'dark', toggle: () => { } });
export function ThemeProvider({ children }) {
    const [theme, setTheme] = useState(() => {
        return localStorage.getItem('theme') ?? 'dark';
    });
    useEffect(() => {
        localStorage.setItem('theme', theme);
        document.documentElement.classList.toggle('dark', theme === 'dark');
    }, [theme]);
    const toggle = () => setTheme((t) => (t === 'dark' ? 'light' : 'dark'));
    return (_jsx(ThemeContext.Provider, { value: { theme, toggle }, children: children }));
}
export function useTheme() {
    return useContext(ThemeContext);
}
