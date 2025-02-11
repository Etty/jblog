import { faker } from "@faker-js/faker";
import { render, screen } from "@testing-library/react";
import PageLink, { Props } from "../../components/PageLink";
// import "@testing-library/jest-dom/extend-expect";

describe("<PageLink />", () => {
  const renderComponent = ({ children, ...props }: Props) =>
    render(<PageLink {...props}>{children}</PageLink>);

  it("should render anchor element by default", () => {
    const text = faker.lorem.sentence();
    const href = faker.internet.url();

    renderComponent({ href, children: text });
    // render(<PageLink href={href}>{text}</PageLink>);

    const el = screen.queryByText(text);

    expect(el?.tagName).toBe("A");
    // expect(el).toHaveAttribute("href", href);
  });

  it("should accept className props", () => {
    const text = faker.lorem.sentence();
    const className = faker.word.noun();

    renderComponent({ className, children: text });

    const el = screen.queryByText(text);

    // expect(el).toHaveClass(className);
  });

  it("should accept className props", () => {
    const text = faker.lorem.sentence();
    const className = faker.word.noun();

    renderComponent({ className, children: text });

    const el = screen.queryByText(text);

    // expect(el).toHaveClass(className);
  });
});
