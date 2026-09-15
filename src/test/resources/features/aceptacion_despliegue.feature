# language: es
Característica: Aceptación de una nueva versión antes de recibir tráfico
  Como equipo de QA
  Quiero validar la versión candidata en el entorno inactivo
  Para asegurarme de que es segura antes de exponerla a usuarios reales

  Escenario: La versión candidata permite iniciar sesión y registrar stock
    Dado que la aplicación candidata está desplegada en el color inactivo
    Cuando un usuario de prueba inicia sesión con credenciales válidas
    Y registra una salida de stock de un producto existente
    Entonces el stock del producto se actualiza correctamente

  Escenario: El endpoint de salud de la versión candidata responde saludable
    Dado que la aplicación candidata está desplegada en el color inactivo
    Cuando se consulta el endpoint "/actuator/health"
    Entonces la respuesta indica el estado "UP"
