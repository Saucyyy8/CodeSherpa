document.addEventListener('DOMContentLoaded', () => {
    const chatbotMessages = document.getElementById('chatbotMessages');
    const chatbotInput = document.getElementById('chatbotInput');
    const chatbotSendButton = document.getElementById('chatbotSendButton');

    chatbotSendButton.addEventListener('click', sendMessage);
    chatbotInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });

    function addMessage(text, sender) {
        const messageElement = document.createElement('div');
        messageElement.classList.add('chat-message', sender === 'user' ? 'user-message' : 'bot-message');
        messageElement.textContent = text;
        chatbotMessages.appendChild(messageElement);
        chatbotMessages.scrollTop = chatbotMessages.scrollHeight;
    }

    async function sendMessage() {
        const message = chatbotInput.value.trim();
        if (!message) return;

        addMessage(message, 'user');
        chatbotInput.value = '';

        // Check for context from script.js
        if (!window.sherpaContext || Object.keys(window.sherpaContext).length === 0) {
            addMessage('Please use the main input to get hints for a problem first. Then you can ask me questions about it.', 'bot');
            return;
        }

        // Get the currently displayed content
        const currentContent = document.getElementById('content-display').textContent;

        try {
            const response = await fetch('/api/problem/chat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                // Send the message and the full context
                body: JSON.stringify({
                    message: message,
                    problemContext: window.sherpaContext,
                    currentHint: currentContent
                }),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Something went wrong');
            }

            const data = await response.json();
            addMessage(data.reply, 'bot');

        } catch (error) {
            console.error('Chatbot error:', error);
            addMessage('Sorry, I encountered an error. Please try again.', 'bot');
        }
    }

    addMessage('Hello! I am CodeSherpa. After you get hints, feel free to ask me any follow-up questions here.', 'bot');
});
