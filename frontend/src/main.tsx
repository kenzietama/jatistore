import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./App.css";
import Login from "./container/auth/Login";

createRoot(document.getElementById("root")!).render(
	<StrictMode>
		<Login />
	</StrictMode>,
);
