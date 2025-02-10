import "./App.css";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap/dist/js/bootstrap.bundle.min.js";
import { Search } from "./components/Search";
import { BrowserRouter, Route, Router, Routes } from "react-router-dom";
import { PostPage } from "./views/PostPage";
import { SearchResultPage } from "./views/SearchResultPage";

function App() {
  return (
    <div className="App">
      <Search />
      <div className="container content">
        <BrowserRouter>
          <Routes>
            <Route path="/:urlKey" element={<PostPage />}></Route>
            <Route path="/search/:query" element={<SearchResultPage />}></Route>
            {/* <Route
              path="/search/:query/:p"
              element={<SearchResultPage />}
            ></Route> */}
          </Routes>
        </BrowserRouter>
      </div>
    </div>
  );
}

export default App;
