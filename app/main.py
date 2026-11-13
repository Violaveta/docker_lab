
from datetime import datetime
from fastapi import FastAPI, HTTPException, status
from pydantic import BaseModel, Field, model_validator

app = FastAPI(
    title="Transport Routes API",
    description="REST API системы электронных перевозочных документов",
    version="1.0.0"
)


class RouteCreate(BaseModel):
    origin: str = Field(..., min_length=2)
    destination: str = Field(..., min_length=2)
    departure_time: datetime
    arrival_time: datetime
    status: str = "planned"

    @model_validator(mode="after")
    def check_times(self):
        if self.arrival_time <= self.departure_time:
            raise ValueError("Arrival time must be later than departure time")
        return self


class Route(RouteCreate):
    id: int


routes: dict[int, Route] = {}
next_id = 1


@app.get("/api/v1/routes/", response_model=list[Route])
def get_routes():
    return list(routes.values())


@app.get("/api/v1/routes/{route_id}", response_model=Route)
def get_route(route_id: int):
    if route_id not in routes:
        raise HTTPException(status_code=404, detail="Route not found")
    return routes[route_id]


@app.post(
    "/api/v1/routes/",
    response_model=Route,
    status_code=status.HTTP_201_CREATED
)
def create_route(route_data: RouteCreate):
    global next_id

    new_route = Route(id=next_id, **route_data.model_dump())
    routes[next_id] = new_route
    next_id += 1

    return new_route


@app.delete(
    "/api/v1/routes/{route_id}",
    status_code=status.HTTP_204_NO_CONTENT
)
def delete_route(route_id: int):
    if route_id not in routes:
        raise HTTPException(status_code=404, detail="Route not found")

    del routes[route_id]
