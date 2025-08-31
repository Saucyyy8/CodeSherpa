# 🏔️ CodeSherpa

**CodeSherpa** is a focused backend service that converts any competitive-programming problem URL into structured, editorial-backed help: three progressive hints, pseudocode, the editorial code and a clear explanation so problem-solvers can focus on reasoning rather than hunting for correct answers.

---

## Why I built CodeSherpa

I created this project because I had difficulties finding hints in many contest links and the code explanation. Each time I had to take a screenshot and ask GPT for doubts and most of the time it used to give wrong answers because of the complexity of Codeforces questions. Therefore I created this project to help give out 3 natural hints for any problem with pseudocode, actual code and explanation of code so any problem solver can focus on understanding the logic of the problem and not waste their time with wrong/stupid solutions of GPT which may be incorrect. The solution my project provides is from the editorial so it is most often correct.

---

## Highlights / Features

* ✳️ **Automated editorial scraping** — give a contest problem URL (Codeforces or similar) and CodeSherpa finds and parses the editorial.
* 🧭 **Three progressive natural-language hints** — hint1, hint2, hint3 that nudge toward the solution without spoiling it.
* 🧾 **Pseudocode** — a language-agnostic algorithm outline.
* 🧩 **Editorial code** — the correct solution derived from the official editorial.
* 🧠 **Code explanation** — block/line-level explanation to help you understand implementation details.
* ✅ **Reliable** — editorial-derived output reduces hallucination/incorrect solutions common from generic LLM prompts.
* 🔁 **JSON-ready** — endpoint returns structured JSON suitable for any frontend (web, VS Code extension, mobile).

---

## Tech stack

* **Backend:** Spring Boot (Java)
* **Scraping & parsing:** Selenium WebDriver, Jsoup
* **AI / Inference:** Fireworks AI platform + `deepseek-v3p1` model
* **Utilities:** Lombok
* **Build:** Maven

---

## Controller (exact)

This is the controller method used by the project to expose the hints endpoint. It accepts a POST request with a URL payload and returns a `HintOutputDto` on success; errors are returned in a simple JSON error wrapper.

```java
@PostMapping("/getHints")
public ResponseEntity<?> url(@Valid @RequestBody UrlInputDto url){
    try{
        HintOutputDto hint = service.getHint(url.getUrl());
        return ResponseEntity.ok(hint);
    }
    catch(Exception e){
        log.error("Error found in getHints endpoint", e);
        Map<String, Object> errorFormat = new HashMap<>();
        errorFormat.put("timestamp: ", LocalDateTime.now());
        errorFormat.put("HttpStatus :", 505);
        errorFormat.put("error :", "Internal Server Error");
        errorFormat.put("message :", e);
        return new ResponseEntity<>(errorFormat,HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

> Notes on the controller
>
> * The controller returns the exact `HintOutputDto` produced by the `service.getHint(...)` call.
> * On exception it logs the error and returns a JSON map with `timestamp`, `HttpStatus`, `error` and `message` fields.
> * Make sure `UrlInputDto` and `HintOutputDto` are documented in the codebase (or include their definitions in `docs/` for consumers).

---

## API Usage

### Endpoint

**POST** `/getHints`

* Request Body (example)

```json
{
  "url": "https://codeforces.com/contest/2132/problem/E"
}
```

* Successful Response (exact sample provided by you)

```json
{
    "hint1": "Power towers grow extremely fast, so direct comparison is infeasible. Instead, compare them by their lexicographical order when the exponents are sorted. This requires comparing the sorted lists of exponents recursively.\n\n###",
    "hint2": "Use a bottom-up topological sort on the tree. For each node, maintain a sorted list (via a persistent segment tree) of its children's rankings, allowing efficient lexicographical comparisons.\n\n###",
    "hint3": "Leverage hashing to represent the sorted list of exponents. Use a priority queue to process nodes from leaves to the root, updating each node's hash value based on its children's hashes and rankings.\n\n---\n\n###",
    "solutionToProblem": "The solution involves processing the tree from leaves to the root (topological sort). For each leaf node, \\( f_u(x) = x \\), which is the smallest power tower. For an internal node \\( u \\), \\( f_u(x) = x^{f_{l[u]}(x)^{f_{r[u]}(x)} \\). To compare two nodes \\( u \\) and \\( v \\), we compare the sorted lists of exponents (the children's power towers) lexicographically. This is done efficiently by storing each node's sorted list as a persistent segment tree that supports hashing. The algorithm starts by pushing all leaves into a priority queue. Nodes are processed in increasing order of their power tower values. When a node is popped, it is assigned a rank, and its parent's dependencies are updated. The parent's hash value is computed by combining the hashes of its children in sorted order. The process continues until the root is processed.\n\n---\n\n###",
    "pseudocode": "1. Read number of test cases \\( T \\).\n2. For each test case:\n   a. Read number of nodes \\( n \\).\n   b. Initialize arrays: \\( l \\), \\( r \\), \\( fa \\), \\( d \\) (in-degree), \\( rk \\), \\( h \\) (hash).\n   c. Initialize a persistent segment tree and a priority queue.\n   d. For each leaf node (in-degree 0), set its hash to 1 and push it into the priority queue.\n   e. While the queue is not empty:\n        i. Pop the smallest node \\( u \\) (based on hash value).\n        ii. If \\( u \\)'s hash is new, increment the total rank counter.\n        iii. Assign \\( u \\) the current rank.\n        iv. Decrement the in-degree of its parent.\n        v. If the parent's in-degree becomes 0, compute its hash by combining the hashes of its children in sorted order (using the segment tree) and push it into the queue.\n3. Output the order of nodes processed.\n\n---\n\n###",
    "code": "#include <bits/stdc++.h>\nusing namespace std;\n\ntypedef unsigned long long ull;\n\nconst int MAXN = 4e5 + 10;\n\null A = mt19937_64(time(0))();\n\ninline ull shift(ull x) {\n    x ^= A;\n    x ^= x << 13;\n    x ^= x >> 7;\n    x ^= x << 11;\n    x ^= A;\n    return x;\n}\n\nstruct Node {\n    int l, r, num;\n    ull val;\n} tree[MAXN * 20];\nint cnt, root[MAXN];\n\nvoid add(int &p, int pre, int l, int r, int k, ull x) {\n    tree[p = ++cnt] = tree[pre];\n    tree[p].val += x;\n    tree[p].num++;\n    if (l == r) return;\n    int mid = (l + r) >> 1;\n    if (k <= mid) add(tree[p].l, tree[pre].l, l, mid, k, x);\n    else add(tree[p].r, tree[pre].r, mid + 1, r, k, x);\n}\n\nbool find(int p, int pre, int l, int r) {\n    if (l == r) return tree[p].num > tree[pre].num;\n    int mid = (l + r) >> 1;\n    if (tree[tree[p].r].val == tree[tree[pre].r].val)\n        return find(tree[p].l, tree[pre].l, l, mid);\n    else\n        return find(tree[p].r, tree[pre].r, mid + 1, r);\n}\n\nint n, l[MAXN], r[MAXN], fa[MAXN], d[MAXN];\nint rk[MAXN], tot, lst;\null h[MAXN];\n\nstruct Compare {\n    bool operator()(int x, int y) {\n        if (h[x] == h[y]) return x > y;\n        return find(root[x], root[y], 1, n);\n    }\n};\n\npriority_queue<int, vector<int>, Compare> q;\n\nvoid upd(int u) {\n    h[u] = h[l[u]] + shift(h[r[u]]);\n    add(root[u], root[l[u]], 1, n, rk[r[u]], shift(h[r[u]]));\n}\n\nint main() {\n    int T;\n    scanf("%d", &T);\n    while (T--) {\n        scanf("%d", &n);\n        cnt = lst = tot = 0;\n        for (int i = 1; i <= n; i++) {\n            root[i] = 0;\n            d[i] = 0;\n        }\n        for (int i = 1; i <= n; i++) {\n            scanf("%d %d", &l[i], &r[i]);\n            if (l[i] && r[i]) d[i] = 2;\n            fa[l[i]] = i;\n            fa[r[i]] = i;\n        }\n        for (int i = 1; i <= n; i++) {\n            if (d[i] == 0) {\n                h[i] = 1;\n                q.push(i);\n            }\n        }\n        while (!q.empty()) {\n            int u = q.top();\n            q.pop();\n            if (h[u] != h[lst]) tot++;\n            lst = u;\n            rk[u] = tot;\n            printf("%d ", u);\n            int parent = fa[u];\n            if (parent == 0) continue;\n            d[parent]--;\n            if (d[parent] == 0) {\n                upd(parent);\n                q.push(parent);\n            }\n        }\n        printf("\\n");\n    }\n    return 0;\n}\n\n---\n\n###",
    "explanationOfCode": "1. **Initialization**: The code reads the number of test cases. For each test case, it reads the number of nodes and initializes arrays for left child, right child, parent, in-degree, rank, and hash value.\n2. **Tree Processing**: For each node, if it has children, its in-degree is set to 2. Leaves (in-degree 0) are pushed into a priority queue with a hash value of 1.\n3. **Priority Queue Processing**: The queue is sorted such that the node with the smallest power tower value (based on hash) is popped first. When popped, the node is assigned a rank. Its parent's in-degree is decremented. If the parent's in-degree becomes zero, its hash is computed by combining the hashes of its children (using a persistent segment tree to maintain sorted order) and it is pushed into the queue.\n4. **Hashing and Segment Tree**: The `shift` function randomizes and mixes the hash. The `add` function updates the segment tree with a new node's hash. The `find` function compares two segment trees to determine which represents a larger sorted list.\n5. **Output**: The order in which nodes are popped from the queue is printed, representing the increasing order of their power tower values."
}

```
