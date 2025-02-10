import { useCallback, useEffect, useState } from "react";
import Post from "../models/Post";
import { Card, Col, Row } from "react-bootstrap";
import SearchResult from "../models/SearchResult";
import { search } from "../actions/SearchAction";
import OnClickOutsideHandler from "../utils/OnClickOutsideHandler";
import React from "react";
import DOMPurify from "isomorphic-dompurify";
import he from "he";

interface SearchFormElements extends HTMLFormControlsCollection {
  q: HTMLInputElement;
}
interface SearchFormElement extends HTMLFormElement {
  readonly elements: SearchFormElements;
}

export const Search = () => {
  const [query, setQuery] = useState("");
  const [suggestions, setSuggestions] = useState<Post[]>([]);
  const [activeSuggestion, setActiveSuggestion] = useState(0);
  const [searchResult, setSearchResult] = useState<SearchResult>();
  const [isOpened, setIsOpened] = useState(false);

  const handleChange = useCallback(
    async (_event: React.ChangeEvent<HTMLInputElement>) => {
      const q = _event.target.value;
      if (_event.target.value !== "") {
        const sr: SearchResult = await search(_event.target.value);
        setSearchResult(sr);
        setIsOpened(true);
        setSuggestions(sr.resultItems.slice(0, 5));
      } else {
        setIsOpened(false);
      }
      setQuery(q);
    },
    []
  );

  const handleKeyDown = (
    event: React.KeyboardEvent<HTMLInputElement>
  ): void => {
    if (!suggestions || !suggestions.length) return;
    const itemsCnt = suggestions.length + 1;
    if (event.key === "ArrowDown") {
      setActiveSuggestion(activeSuggestion + 1);
      if (activeSuggestion >= itemsCnt) {
        setActiveSuggestion(1);
      }
    } else if (event.key === "ArrowUp") {
      setActiveSuggestion(activeSuggestion - 1);
      if (activeSuggestion <= 1) {
        setActiveSuggestion(itemsCnt);
      }
    } else if (event.key === "Enter") {
      event.preventDefault();
      if (!searchResult!.searchResultsLink) {
        return;
      }
      if (suggestions[activeSuggestion - 1]) {
        window.location.replace("/" + suggestions[activeSuggestion - 1].urlKey);
      } else {
        window.location.replace(searchResult!.searchResultsLink);
      }

      setIsOpened(false);
      setActiveSuggestion(0);
    }
  };

  const handleMouseMove = (value: number) => {
    setActiveSuggestion(value + 1);
  };

  function handleClick() {
    if (suggestions[activeSuggestion - 1]) {
      window.location.replace("/" + suggestions[activeSuggestion - 1].urlKey);
    } else {
      window.location.replace(searchResult!.searchResultsLink);
    }

    setIsOpened(false);
    setActiveSuggestion(0);
  }

  function handleSubmit(_event: React.FormEvent<SearchFormElement>) {
    _event.preventDefault();
    if (
      _event.currentTarget.elements.q.value &&
      searchResult!.searchResultsLink
    ) {
      window.location.replace(searchResult!.searchResultsLink);
    }
  }

  const clickOutsideHandler = new OnClickOutsideHandler(
    document.getElementById("searchQuery")!,
    () => {
      setIsOpened(false);
      setActiveSuggestion(0);
    }
  );

  useEffect(() => {
    document
      .querySelectorAll(".searchresult-item")
      .forEach((elStaticParent) => {
        elStaticParent.addEventListener("click", (evt) => handleClick());
      });
    console.log(document.querySelectorAll(".searchresult-item"));
  }, [searchResult]);

  return (
    <div className="container col-12 col-md-12">
      <form autoComplete="off" className="d-flex" onSubmit={handleSubmit}>
        <div className="autocomplete col-sm-11 col-11">
          <input
            id="searchQuery"
            type="search"
            name="q"
            placeholder="Search..."
            className="form-control me-2"
            aria-label="Search"
            value={query}
            onChange={handleChange}
            onKeyDown={handleKeyDown}
          />
        </div>

        <button type="submit" className="btn btn-primary col-sm-1 col-1">
          Go
        </button>
      </form>
      {isOpened ? (
        <Card>
          <Card.Body style={{ padding: "0" }}>
            {!searchResult?.resultItems.length &&
            query.length &&
            !suggestions.length ? (
              <Row className="no-search-result">
                <Col>Nothing to show</Col>
              </Row>
            ) : (
              <>
                {suggestions.map(
                  (
                    { title, description, postIdentifier, urlKey }: Post,
                    index
                  ) => (
                    <Row
                      key={index}
                      className={`searchresult-item ${
                        index === activeSuggestion - 1
                          ? "autocomplete-active"
                          : ""
                      }`}
                      // onClick={handleClick}
                      onMouseEnter={() => handleMouseMove(index)}
                    >
                      <Col className="border-bottom">
                        <div
                          className="text-center fs-5"
                          dangerouslySetInnerHTML={{
                            __html:
                              searchResult?.highlights[postIdentifier] &&
                              searchResult?.highlights[postIdentifier]["title"]
                                ? DOMPurify.sanitize(
                                    searchResult?.highlights[postIdentifier][
                                      "title"
                                    ][0]
                                  )
                                : title,
                          }}
                        />

                        <div
                          dangerouslySetInnerHTML={{
                            __html:
                              searchResult?.highlights[postIdentifier] &&
                              searchResult?.highlights[postIdentifier][
                                "description"
                              ]
                                ? DOMPurify.sanitize(
                                    he.decode(
                                      Array(
                                        searchResult?.highlights[
                                          postIdentifier
                                        ]["description"]
                                      ).join("...")
                                    ),
                                    {
                                      ALLOWED_TAGS: ["em"],
                                    }
                                  ) + "..."
                                : DOMPurify.sanitize(he.decode(description), {
                                    ALLOWED_TAGS: [],
                                  }).substring(0, 50) + "...",
                          }}
                        />
                      </Col>
                    </Row>
                  )
                )}
                <Row
                  key={suggestions.length}
                  className={`searchresult-item ${
                    suggestions.length === activeSuggestion - 1
                      ? "autocomplete-active"
                      : ""
                  }`}
                  // onClick={handleClick}
                  onMouseEnter={() => handleMouseMove(suggestions.length + 1)}
                >
                  <Col>
                    <div className="text-end">
                      {`View all results (${searchResult?.resultsCount}) >>> `}
                    </div>
                  </Col>
                </Row>
              </>
            )}
          </Card.Body>
        </Card>
      ) : (
        <></>
      )}
    </div>
  );
};
