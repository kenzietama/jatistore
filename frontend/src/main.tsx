import { StrictMode } from "react";
import { createRoot } from "react-dom/client";

const App = () => {
	return <div>HELLO WORLD</div>;
};

createRoot(document.getElementById("root")!).render(
	<StrictMode>
		<App />
	</StrictMode>,
);
