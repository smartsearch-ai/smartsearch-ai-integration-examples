"""Read curl's SSE bytes; only a valid completed payload is a successful answer.

Uses Python's standard library. Output includes provisional deltas and the final JSON result,
but failures discard untrusted error messages. Accepts fragmented UTF-8, LF/CRLF/bare CR.
"""

import io
import json
import sys


def main():
    """Parse framed events and require final completion, retaining chat session context."""
    event = "message"
    data = []
    size = 0
    session = None
    # Explicit newline=None accepts bare CR as well as LF/CRLF; reads are bounded.
    stream = io.TextIOWrapper(
        sys.stdin.buffer, encoding="utf-8", errors="strict", newline=None
    )
    while True:
        line = stream.readline(1024 * 1024 + 1)
        if not line:
            break
        size += len(line.encode("utf-8"))
        if size > 1024 * 1024:
            raise ValueError("event too large")
        line = line.rstrip("\r\n")
        if not line:
            if data:
                envelope = json.loads("\n".join(data))
                if not isinstance(envelope, dict):
                    raise ValueError("invalid event")
                payload = envelope.get("payload", {})
                if not isinstance(payload, dict):
                    raise ValueError("invalid payload")
                if event in ("error", "run.failed"):
                    raise ValueError("failed run")
                if event == "run.started" and isinstance(
                    payload.get("session_id"), str
                ):
                    session = payload["session_id"]
                if event == "answer.delta":
                    print(str(payload.get("text", "")), end="", flush=True)
                if event == "run.completed":
                    if payload.get("status") != "COMPLETED":
                        raise ValueError("invalid completion")
                    if session and "session_id" not in payload:
                        payload["session_id"] = session
                    print("\nFinal result:")
                    print(json.dumps(payload, ensure_ascii=False, indent=2))
                    return
            event, data, size = "message", [], 0
        elif line.startswith("event:"):
            event = line[6:].removeprefix(" ")
        elif line.startswith("data:"):
            data.append(line[5:].removeprefix(" "))
    raise ValueError("stream ended before completion")


if __name__ == "__main__":
    try:
        main()
    except Exception:
        print(
            "ERROR Incomplete or failed SSE stream; discard provisional text",
            file=sys.stderr,
        )
        sys.exit(1)
