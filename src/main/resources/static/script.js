document.addEventListener('DOMContentLoaded', () => {
    const urlInput = document.getElementById('urlInput');
    const getHintsButton = document.getElementById('getHintsButton');
    const loader = document.getElementById('loader');
    const resultsContainer = document.getElementById('results');
    const buttonsContainer = document.getElementById('buttons-container');
    const contentDisplay = document.getElementById('content-display');
    const errorDisplay = document.getElementById('error');

    // Make context available to chatbot.js
    window.sherpaContext = {};

    getHintsButton.addEventListener('click', fetchHints);

    async function fetchHints() {
        const url = urlInput.value;
        if (!url) {
            showError("Please enter a valid Codeforces problem URL.");
            return;
        }

        // Reset UI and context
        loader.classList.remove('hidden');
        resultsContainer.classList.add('hidden');
        errorDisplay.classList.add('hidden');
        buttonsContainer.innerHTML = '';
        contentDisplay.innerHTML = '';
        window.sherpaContext = {}; // Clear previous context

        try {
            const response = await fetch('/api/problem/getHints', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ url: url }),
            });

            if (!response.ok) {
                const errorData = await response.json();
                const serverMessage = errorData.message || (typeof errorData.error === 'string' ? errorData.error : 'An unknown error occurred.');
                throw new Error(serverMessage);
            }

            const data = await response.json();
            window.sherpaContext = data; // Store context globally
            displayResults(data);

        } catch (error) {
            console.error('Error fetching hints:', error);
            showError(`Failed to fetch hints: ${error.message}`);
        } finally {
            loader.classList.add('hidden');
        }
    }

    function displayResults(data) {
        resultsContainer.classList.remove('hidden');
        const contentMap = {
            "Hint 1": data.hint1,
            "Hint 2": data.hint2,
            "Hint 3": data.hint3,
            "Solution": data.solutionToProblem,
            "Pseudocode": data.pseudocode,
            "Code": data.code,
            "Explanation": data.explanationOfCode
        };

        let firstButton = null;

        for (const [key, value] of Object.entries(contentMap)) {
            if (value && value.trim()) {
                const button = document.createElement('button');
                button.textContent = key;
                button.addEventListener('click', () => {
                    contentDisplay.textContent = value;
                    document.querySelectorAll('.buttons-container button').forEach(btn => btn.classList.remove('active'));
                    button.classList.add('active');
                });
                buttonsContainer.appendChild(button);
                if (!firstButton) {
                    firstButton = button;
                }
            }
        }

        if (firstButton) {
            firstButton.click();
        } else {
            contentDisplay.textContent = 'No hints or solutions could be extracted. The AI may not have been able to process this problem.';
        }
    }

    function showError(message) {
        errorDisplay.textContent = message;
        errorDisplay.classList.remove('hidden');
    }
});
