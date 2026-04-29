import { setupServer } from 'msw/node';

// Tests register handlers per scenario so each request expectation stays close to its assertion.
export const server = setupServer();
