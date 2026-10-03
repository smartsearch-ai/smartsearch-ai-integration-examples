/** Help/list never read connection settings or call the network. */
import { examples } from "./registry.js";
const [name, ...args] = process.argv.slice(2);
if (!name || name === "--help" || name === "--list") {
  console.log("Usage: npm run example -- <example_name> [args...]");
  for (const example of examples)
    console.log(`${example.step}. ${example.name}: ${example.description}`);
} else {
  const example = examples.find((value) => value.name === name);
  if (!example) {
    console.error("Unknown example; use npm run list");
    process.exitCode = 2;
  } else if (args.includes("--help"))
    console.log(`${example.step}. ${example.name}: ${example.description}`);
  else {
    try {
      await (await example.load()).main(args);
    } catch (error) {
      // Unexpected exceptions may carry credentials/request data. Discard their raw messages.
      const { ExampleError } = await import("./client.js");
      const { ConfigurationError } = await import("./common.js");
      console.error(
        error instanceof ExampleError || error instanceof ConfigurationError
          ? `ERROR ${error.message}`
          : "ERROR Invalid configuration/input or unexpected failure; check settings and example prerequisites",
      );
      process.exitCode = 1;
    }
  }
}
