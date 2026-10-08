const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// --- SECURITY BOUNDS TESTS ---

test("Unauthenticated user: cannot read restaurants", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("restaurants").get());
});

test("Unauthenticated user: cannot write restaurant", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("restaurants").doc("rest_1").set({
    id: "rest_1",
    ownerId: "someone",
    name: "Cafe 1",
    currency: "Rs."
  }));
});

test("Authenticated user: can create restaurant if ownerId matches", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("restaurants").doc("rest_alice").set({
    id: "rest_alice",
    ownerId: ALICE_UID,
    name: "Alice Cafe",
    currency: "Rs."
  }));
});

test("Authenticated user: cannot create restaurant for someone else", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("restaurants").doc("rest_bob").set({
    id: "rest_bob",
    ownerId: BOB_UID,
    name: "Bob Cafe",
    currency: "Rs."
  }));
});

test("Authenticated user: can manage subcollections under restaurant", async () => {
  // Seed restaurant
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("restaurants").doc("rest_alice").set({
      id: "rest_alice",
      ownerId: ALICE_UID,
      name: "Alice Cafe",
      currency: "Rs."
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("restaurants").doc("rest_alice").collection("categories").doc("cat_1").set({
    id: "cat_1",
    restaurantId: "rest_alice",
    name: "Momo",
    isActive: true
  }));

  await assertSucceeds(aliceDb.collection("restaurants").doc("rest_alice").collection("tables").doc("table_1").set({
    id: "table_1",
    restaurantId: "rest_alice",
    tableNumber: "T-01",
    capacity: 4,
    floor: "Ground Floor",
    status: "AVAILABLE"
  }));
});
