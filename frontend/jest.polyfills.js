const { Blob, File } = require('node:buffer');
const { ReadableStream, TransformStream } = require('node:stream/web');
const { TextDecoder, TextEncoder } = require('node:util');
const { MessageChannel, MessagePort } = require('node:worker_threads');

Object.defineProperties(globalThis, {
  Blob: { value: Blob, configurable: true },
  File: { value: File, configurable: true },
  MessageChannel: { value: MessageChannel, configurable: true },
  MessagePort: { value: MessagePort, configurable: true },
  ReadableStream: { value: ReadableStream, configurable: true },
  TextDecoder: { value: TextDecoder, configurable: true },
  TextEncoder: { value: TextEncoder, configurable: true },
  TransformStream: { value: TransformStream, configurable: true },
});

const { fetch, FormData, Headers, Request, Response } = require('undici');

Object.defineProperties(globalThis, {
  fetch: { value: fetch, configurable: true },
  FormData: { value: FormData, configurable: true },
  Headers: { value: Headers, configurable: true },
  Request: { value: Request, configurable: true },
  Response: { value: Response, configurable: true },
});

if (typeof globalThis.BroadcastChannel === 'undefined') {
  class MockBroadcastChannel {
    constructor() {
      this.onmessage = null;
    }

    close() {}

    postMessage() {}

  }

  Object.defineProperty(globalThis, 'BroadcastChannel', {
    value: MockBroadcastChannel,
    configurable: true,
  });
}
