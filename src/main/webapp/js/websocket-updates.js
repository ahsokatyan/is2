// WebSocket-клиент для обновления таблиц
let websocket;
let reconnectInterval;

function connectWebSocket() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUri = protocol + '//' + window.location.host + '/is2-1.0-SNAPSHOT/creatures-updates';
    
    websocket = new WebSocket(wsUri);
    
    websocket.onopen = function(event) {
        console.log('WebSocket connected');
        // Очищаем возможный предыдущий интервал переподключения
        if (reconnectInterval) {
            clearInterval(reconnectInterval);
            reconnectInterval = null;
        }
    };
    
    websocket.onmessage = function(event) {
        const message = event.data;
        console.log('Received WebSocket message:', message);
        
        // Обработка сообщений в формате: TYPE:ID
        const [type, id] = message.split(':');
        const entityId = parseInt(id);
        
        if (!isNaN(entityId)) {
            switch(type) {
                case 'CREATURE_CREATED':
                case 'CREATURE_UPDATED':
                case 'CREATURE_DELETED':
                    updateCreaturesTable(type, entityId);
                    break;
                case 'CITY_CREATED':
                case 'CITY_UPDATED':
                case 'CITY_DELETED':
                    updateCitiesTable(type, entityId);
                    break;
                case 'RING_CREATED':
                case 'RING_UPDATED':
                case 'RING_DELETED':
                    updateRingsTable(type, entityId);
                    break;
            }
        }
    };
    
    websocket.onclose = function(event) {
        console.log('WebSocket disconnected, attempting to reconnect...');
        // Пытаемся переподключиться каждые 5 секунд
        reconnectInterval = setInterval(() => {
            connectWebSocket();
        }, 5000);
    };
    
    websocket.onerror = function(error) {
        console.error('WebSocket error:', error);
    };
}

function updateCreaturesTable(eventType, id) {
    // Проверяем, находимся ли мы на странице со списком существ
    const creatureTableForm = document.getElementById('listForm');
    if (creatureTableForm) {
        // Обновляем страницу через небольшую задержку, чтобы дать данным обновиться
        setTimeout(() => {
            // Проверяем, не находится ли сейчас страница в процессе обновления
            if (!isPageUpdating()) {
                location.reload();
            }
        }, 100);
    }
}

function updateCitiesTable(eventType, id) {
    // Проверяем, находимся ли мы на странице со списком городов
    const cityTableForm = document.getElementById('listForm');
    if (cityTableForm) {
        // Обновляем страницу через задержку, чтобы дать данным обновиться
        setTimeout(() => {
            // Проверяем, не находится ли сейчас страница в процессе обновления
            if (!isPageUpdating()) {
                location.reload();
            }
        }, 100);
    }
}

function updateRingsTable(eventType, id) {
    // Проверяем, находимся ли мы на странице со списком колец
    const ringTableForm = document.getElementById('listForm');
    if (ringTableForm) {
        // Обновляем страницу через задержку, чтобы дать данным обновиться
        setTimeout(() => {
            // Проверяем, не находится ли сейчас страница в процессе обновления
            if (!isPageUpdating()) {
                location.reload();
            }
        }, 100);
    }
}

function isPageUpdating() {
    // Проверяем, не находится ли страница в процессе обновления
    return false;
}

// Инициализация WebSocket при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    connectWebSocket();
});

// Закрываем WebSocket при анлоаде страницы
window.addEventListener('beforeunload', function() {
    if (websocket) {
        websocket.close();
    }
    if (reconnectInterval) {
        clearInterval(reconnectInterval);
    }
});