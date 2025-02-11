import { render, screen, fireEvent } from "@testing-library/react";
import Pagination, { Props } from "../../components/Pagination";
import React from "react";

describe("<Pagination />", () => {
  const renderComponent = (props: Props) => render(<Pagination {...props} />);

  it("should render nav element", () => {
    const lastPage = 5;

    renderComponent({
      lastPage,
      currentPage: 1,
      maxLength: 7,
      setCurrentPage: jest.fn(),
    });

    const navEl = screen.queryByLabelText("Pagination");
    const navElChildTextContents = [
      "Previous",
      "1",
      "2",
      "3",
      "4",
      "5",
      "Next",
    ];

    expect(navEl?.tagName).toBe("NAV");
    expect(navEl?.childElementCount).toBe(lastPage + 2);
    expect(navEl?.childElementCount).toBe(navElChildTextContents.length);
  });

  it("should render ellipsis", () => {
    renderComponent({
      currentPage: 5,
      lastPage: 10,
      maxLength: 7,
      setCurrentPage: jest.fn(),
    });

    const navEl = screen.queryByLabelText("Pagination");
    const navElChildTextContents = [
      "Previous",
      "1",
      "...",
      "4",
      "5",
      "6",
      "...",
      "10",
      "Next",
    ];

    expect(navEl?.childElementCount).toBe(navElChildTextContents.length);
  });

  it("should handle PageLink on click", () => {
    const setCurrentPageMock = jest.fn();
    const currentPage = 5;

    renderComponent({
      currentPage,
      lastPage: 10,
      maxLength: 7,
      setCurrentPage: setCurrentPageMock,
    });

    expect(setCurrentPageMock).not.toBeCalled();

    const previousEl = screen.queryByText("Previous") as HTMLElement;

    fireEvent.click(previousEl);

    expect(setCurrentPageMock).toBeCalledTimes(1);
    expect(setCurrentPageMock).toBeCalledWith(currentPage - 1);

    const btnItems = [
      {
        text: "Previous",
        pageNum: currentPage - 1,
      },
      {
        text: "Next",
        pageNum: currentPage + 1,
      },
      {
        text: "4",
        pageNum: 4,
      },
    ];

    renderComponent({
      currentPage,
      lastPage: 10,
      maxLength: 7,
      setCurrentPage: setCurrentPageMock,
    });

    expect(setCurrentPageMock).not.toBeCalled();

    btnItems.forEach(({ text, pageNum }) => {
      const btnEl = screen.queryByText(text) as HTMLElement;

      fireEvent.click(btnEl);

      expect(setCurrentPageMock).toBeCalledTimes(1);
      expect(setCurrentPageMock).toBeCalledWith(pageNum);

      setCurrentPageMock.mockClear();
    });
  });
});
