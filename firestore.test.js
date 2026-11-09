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

// 1. Unauthenticated user cannot read or write user profiles
test("Unauthenticated user: cannot read users", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

// 2. Authenticated user can create their own profile
test("Authenticated user: can create own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set({
    userId: ALICE_UID,
    username: "AliceGamer",
    phone: "0911223344",
    playingTime: "8:00 O’clock Day",
    wallet: 150,
    createdAt: new Date(),
  }));
});

// 3. Authenticated user cannot write another user's profile
test("Authenticated user: cannot write another user profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).set({
    userId: BOB_UID,
    username: "BobGamer",
    phone: "0922334455",
    playingTime: "8:00 O’clock Day",
    wallet: 100,
    createdAt: new Date(),
  }));
});

// 4. Authenticated user can submit a valid deposit
test("Authenticated user: can submit deposit", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("deposits").doc("dep_1001").set({
    depositId: "dep_1001",
    userId: ALICE_UID,
    username: "AliceGamer",
    amount: 100,
    paymentMethod: "telebirr",
    receiptId: "TB12345678",
    status: "pending",
    createdAt: new Date(),
  }));
});

// 5. Authenticated user can submit a valid withdrawal
test("Authenticated user: can submit withdrawal", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("withdrawals").doc("wth_2001").set({
    withdrawId: "wth_2001",
    userId: ALICE_UID,
    username: "AliceGamer",
    amount: 50,
    paymentMethod: "cbe",
    targetAccount: "0939383425",
    status: "pending",
    createdAt: new Date(),
  }));
});

// 6. Game room read by authenticated user
test("Authenticated user: can read game rooms", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("game_rooms").doc("room_active").set({
      roomId: "room_active",
      slot: "8:00 O’clock Day",
      round: 1,
      status: "playing",
      calledNumbers: [1, 25, 42],
      prizePool: 300,
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("game_rooms").doc("room_active").get());
});
